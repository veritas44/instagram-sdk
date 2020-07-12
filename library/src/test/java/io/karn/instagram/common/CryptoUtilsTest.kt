package io.karn.instagram.common

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class CryptoUtilsTest {

    companion object {
        /**
        -----BEGIN PUBLIC KEY-----
        MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/3tuV0vSpTBRKepf4QEmNzER
        hoTRCzd3L907QbvqyONCPX7JDX4Pw2lNEdqQwolHNa3wkLvyfYq0eD+sexWIjBiA
        5cTmqtbR21j1Yiq3E0Y8JFs/p1Ks7wpwnwRjf/UfR0Si36xMEtVP1PlNBUQkAZjc
        3bETB2ynuiWPEaB5uwIDAQAB
        -----END PUBLIC KEY-----
         */
        const val TEST_PUBLIC_KEY = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlHZk1BMEdDU3FHU0liM0RRRUJBUVVBQTRHTkFEQ0JpUUtCZ1FDby8zdHVWMHZTcFRCUktlcGY0UUVtTnpFUgpob1RSQ3pkM0w5MDdRYnZxeU9OQ1BYN0pEWDRQdzJsTkVkcVF3b2xITmEzd2tMdnlmWXEwZUQrc2V4V0lqQmlBCjVjVG1xdGJSMjFqMVlpcTNFMFk4SkZzL3AxS3M3d3B3bndSamYvVWZSMFNpMzZ4TUV0VlAxUGxOQlVRa0FaamMKM2JFVEIyeW51aVdQRWFCNXV3SURBUUFCCi0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQ=="

        /**
        -----BEGIN RSA PRIVATE KEY-----
        MIICXQIBAAKBgQCo/3tuV0vSpTBRKepf4QEmNzERhoTRCzd3L907QbvqyONCPX7J
        DX4Pw2lNEdqQwolHNa3wkLvyfYq0eD+sexWIjBiA5cTmqtbR21j1Yiq3E0Y8JFs/
        p1Ks7wpwnwRjf/UfR0Si36xMEtVP1PlNBUQkAZjc3bETB2ynuiWPEaB5uwIDAQAB
        AoGAIyWvSA2DyXVtjRPImNQ05vvHiruNV+SbToB35GERcg0Bpr8fZTUXKLQdbFfw
        OvcakeLKICQZ0p7lgA8aPwcCPSIyBAtU55xSt3m4Bs07PMk0H/TqN3fXM0taOCwc
        z8SkWEunvO0ecjPU7GMkWPt+khVsWTQ0VGuS6ZTfZaTSKfECQQD0TYaoFD/6v6WZ
        rUYbs9Fr50h541PViR4k3Ahtvx97ohGX5YMQ+4IubcnV1SjwWJGMCVkTemeu58aw
        mbBMl1itAkEAsRbthNs7MZFyuAHY32zUXbC+rkv2v+TrtpT3V+fYhLd7hdMvyyvc
        mA2D94oWfIfY2sm6P2KR9EXTGuH1J2HhBwJBAM70deQg82oZu9GJpeCF3hzImu8W
        kshMFgsnksRVqffz/W9t8DAMv+VRADBzGPEyNmoo6RrFENxsYDbumxjnBSkCQE+B
        68Hqa/klREw5CBXH1tD5uaKnmHEvZmTjHOnqvdtYRa0f1CVz2+aoqsdIyC/BDius
        xmIO71iswMshB+BWoaECQQCZhjAps3CyLo0c4INAzjJVSR9kb5c0/MhNLudQrOjn
        d5ANim1bfefv40K04hP4PYZsU3Rxma6PdMOFelf16BBh
        -----END RSA PRIVATE KEY-----
         */
        const val TEST_PRIVATE_KEY = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlDWFFJQkFBS0JnUUNvLzN0dVYwdlNwVEJSS2VwZjRRRW1OekVSaG9UUkN6ZDNMOTA3UWJ2cXlPTkNQWDdKCkRYNFB3MmxORWRxUXdvbEhOYTN3a0x2eWZZcTBlRCtzZXhXSWpCaUE1Y1RtcXRiUjIxajFZaXEzRTBZOEpGcy8KcDFLczd3cHdud1JqZi9VZlIwU2kzNnhNRXRWUDFQbE5CVVFrQVpqYzNiRVRCMnludWlXUEVhQjV1d0lEQVFBQgpBb0dBSXlXdlNBMkR5WFZ0alJQSW1OUTA1dnZIaXJ1TlYrU2JUb0IzNUdFUmNnMEJwcjhmWlRVWEtMUWRiRmZ3Ck92Y2FrZUxLSUNRWjBwN2xnQThhUHdjQ1BTSXlCQXRVNTV4U3QzbTRCczA3UE1rMEgvVHFOM2ZYTTB0YU9Dd2MKejhTa1dFdW52TzBlY2pQVTdHTWtXUHQra2hWc1dUUTBWR3VTNlpUZlphVFNLZkVDUVFEMFRZYW9GRC82djZXWgpyVVliczlGcjUwaDU0MVBWaVI0azNBaHR2eDk3b2hHWDVZTVErNEl1YmNuVjFTandXSkdNQ1ZrVGVtZXU1OGF3Cm1iQk1sMWl0QWtFQXNSYnRoTnM3TVpGeXVBSFkzMnpVWGJDK3JrdjJ2K1RydHBUM1YrZlloTGQ3aGRNdnl5dmMKbUEyRDk0b1dmSWZZMnNtNlAyS1I5RVhUR3VIMUoySGhCd0pCQU03MGRlUWc4Mm9adTlHSnBlQ0YzaHpJbXU4Vwprc2hNRmdzbmtzUlZxZmZ6L1c5dDhEQU12K1ZSQURCekdQRXlObW9vNlJyRkVOeHNZRGJ1bXhqbkJTa0NRRStCCjY4SHFhL2tsUkV3NUNCWEgxdEQ1dWFLbm1IRXZabVRqSE9ucXZkdFlSYTBmMUNWejIrYW9xc2RJeUMvQkRpdXMKeG1JTzcxaXN3TXNoQitCV29hRUNRUUNaaGpBcHMzQ3lMbzBjNElOQXpqSlZTUjlrYjVjMC9NaE5MdWRRck9qbgpkNUFOaW0xYmZlZnY0MEswNGhQNFBZWnNVM1J4bWE2UGRNT0ZlbGYxNkJCaAotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo="
    }

    @Test
    fun test_ExtractionOfRSAPublicKeyBodyFromEncodedString() {
        val rsaPublicKeyContents = CryptoUtils.extractBase64PublicKeyContents(TEST_PUBLIC_KEY)

        assertEquals("""
            MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/3tuV0vSpTBRKepf4QEmNzER
            hoTRCzd3L907QbvqyONCPX7JDX4Pw2lNEdqQwolHNa3wkLvyfYq0eD+sexWIjBiA
            5cTmqtbR21j1Yiq3E0Y8JFs/p1Ks7wpwnwRjf/UfR0Si36xMEtVP1PlNBUQkAZjc
            3bETB2ynuiWPEaB5uwIDAQAB""".trimIndent().replace("\n", ""), rsaPublicKeyContents)
    }

    @Test
    fun testBytes() {
        assertEquals("0001", CryptoUtils.bytesToHex(ByteBuffer.allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(256)
                .array()))
    }
}
