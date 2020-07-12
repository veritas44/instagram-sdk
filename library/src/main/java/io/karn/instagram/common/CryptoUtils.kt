package io.karn.instagram.common

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


internal object CryptoUtils {

    private const val GCM_AUTH_TAG_BYTE_SIZE = 16

    private const val PUBLIC_RSA_PREFIX = "-----BEGIN PUBLIC KEY-----"
    private const val PUBLIC_RSA_SUFFIX = "-----END PUBLIC KEY-----"

    private fun md5Hex(source: String): String = digest("MD5", source)

    private fun digest(codec: String, source: String): String {
        val digest = MessageDigest.getInstance(codec)
        val digestBytes = digest.digest(source.toByteArray(Charset.forName("UTF-8")))

        return bytesToHex(digestBytes)
    }

    fun bytesToHex(bytes: ByteArray): String {
        val builder = StringBuilder()

        bytes.forEach { builder.append(String.format("%02x", it)) }

        return builder.toString()
    }

    fun generateAndroidId(seed: String): String {
        val seed = md5Hex(seed)
        val volatileSeed = "12345"

        return "android-" + md5Hex(seed + volatileSeed).substring(0, 16)
    }

    fun createJazoest(input: String): String {
        val inputBytes = input.toByteArray(Charsets.US_ASCII)

        val sum = inputBytes.sumBy { (it.toInt() and 0xFF) }

        return "2$sum"
    }

    // Diagram: https://camo.githubusercontent.com/ff052656777d9ba16194d298e688f7ed89811c6b/68747470733a2f2f692e706f7374696d672e63632f685068744b6644422f556e7469746c65642d4469616772616d2d322e706e67
    // Ref: https://github.com/dilame/instagram-private-api/blob/master/src/repositories/account.repository.ts#L77
    fun encryptPassword(key: String, keyId: Int, time: String, password: String): String {

        // RSA
        val randomKey = Random.Default.nextBytes(32)
        val rsaEncrypted = encryptRSA(key, randomKey)!!

        // AES/GCM
        val iv = Random.Default.nextBytes(12)
        val (aesEncrypted, tag) = encryptAESGCM(randomKey, time.toByteArray(), iv, password.toByteArray())

        val sizeBuffer = ByteBuffer.allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(rsaEncrypted.size.toShort())
                .array()

        val encryptedAll = ByteArrayOutputStream()
        // println("ID: " + bytesToHex(byteArrayOf(1.toByte())))
        encryptedAll.write(1)
        // println("Public Key ID: " + bytesToHex(byteArrayOf(keyId.toByte())))
        encryptedAll.write(keyId)
        // println("IV: " + bytesToHex(iv))
        encryptedAll.write(iv)
        // println("RSA length LE: " + bytesToHex(sizeBuffer))
        encryptedAll.write(sizeBuffer)
        // println("RSA Encrypted Key: " + bytesToHex(rsaEncrypted))
        encryptedAll.write(rsaEncrypted)
        // println("AES GCM TAG: " + bytesToHex(tag))
        encryptedAll.write(tag)
        // println("AES GCM Encrypted Password: " + bytesToHex(aesEncrypted))
        encryptedAll.write(aesEncrypted)

        return Base64.encode(encryptedAll.toByteArray(), Base64.DEFAULT).toString(Charsets.UTF_8).replace("\n", "")
    }

    internal fun extractBase64PublicKeyContents(base64EncodedKey: String): String {
        val rsaPublicKey = Base64.decode(base64EncodedKey, Base64.DEFAULT).toString(Charsets.UTF_8)
                .replace("\n", "")

        // Validate that the format is correct.

        if (!rsaPublicKey.startsWith(PUBLIC_RSA_PREFIX) || !rsaPublicKey.endsWith(PUBLIC_RSA_SUFFIX)) {
            throw IllegalArgumentException("Base64 decoded string must be padded with standard PEM key lines.")
        }

        return rsaPublicKey
                .replace(PUBLIC_RSA_PREFIX, "")
                .replace(PUBLIC_RSA_SUFFIX, "")
    }

    private fun encryptRSA(key: String, value: ByteArray): ByteArray? {
        val rsaKey = getRSAPublicKey(key)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, rsaKey)

        return cipher.doFinal(value)
    }

    private fun getRSAPublicKey(base64EncodedKey: String): PublicKey {
        try {
            val key = Base64.decode(base64EncodedKey, Base64.DEFAULT)
                    .toString(Charsets.UTF_8)
                    .replace("\n", "")
                    .replace(PUBLIC_RSA_PREFIX, "")
                    .replace(PUBLIC_RSA_SUFFIX, "")

            val keySpec = X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))

            return KeyFactory.getInstance("RSA").generatePublic(keySpec)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun encryptAESGCM(key: ByteArray, aad: ByteArray, iv: ByteArray, value: ByteArray): Pair<ByteArray, ByteArray> {

        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")

            val secretKey = SecretKeySpec(key, "AES")

            val gcmParameterSpec = GCMParameterSpec(GCM_AUTH_TAG_BYTE_SIZE * 8, iv)

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
            cipher.updateAAD(aad)

            val cipherText = cipher.doFinal(value)

            // println(cipherText.toString(Charsets.UTF_8).length)

            val cipherTextTrimmed = Arrays.copyOfRange(cipherText, 0, cipherText.size - GCM_AUTH_TAG_BYTE_SIZE)

            // println(cipherTextTrimmed.toString(Charsets.UTF_8).length)

            val tag = Arrays.copyOfRange(cipherText, cipherText.size - GCM_AUTH_TAG_BYTE_SIZE, cipherText.size)

            return Pair(cipherTextTrimmed, tag)
        } catch (e: Exception) {
            throw e
        }
    }
}
