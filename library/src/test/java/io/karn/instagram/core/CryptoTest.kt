package io.karn.instagram.core

import io.karn.instagram.TestBase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals


@RunWith(RobolectricTestRunner::class)
class CryptoTest : TestBase() {

    /**
     * Calling the generateAndroidId function should yield the same results
     */
    @Test
    fun test_androidIdGeneration() {
        val instanceId = "flkrFMziAva"
        val expected = "android-79ce56c6d1006ab0"
        assertEquals(expected, Crypto.generateAndroidId(instanceId))

        assertEquals(expected, Crypto.generateAndroidId(instanceId))
    }


    /**
     * Calling the generateDeviceId function should yield the same results
     */
    @Test
    fun test_deviceIdGeneration() {
        val instanceId = "flkrFMziAva"
        val expected = "10872cce-904e-3543-acd6-2ce750f496dd"
        assertEquals(expected, Crypto.generateUUID(instanceId))

        assertEquals(expected, Crypto.generateUUID(instanceId))
    }
}
