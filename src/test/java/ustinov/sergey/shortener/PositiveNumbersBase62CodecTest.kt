package ustinov.sergey.shortener

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils.isAlphanumeric
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class PositiveNumbersBase62CodecTest {

    // 9,223,372,036,854,775,808
    companion object {
        val ENCODED_CONSTANTS = mapOf(
            0L to "0",
            85L to "1N",
            192L to "36",
            1_882L to "UM",
            49_915L to "Cz5",
            999_117L to "4Bun",
            47_582_901L to "3DeU9",
            557_903_864L to "bkuHQ",
            7_980_002_114L to "8i3H5W",
            15_227_374_999L to "GcWVqZ",
            891_643_664_775L to "FhGfwtr",
            9_000_551_473_829L to "2YSVIjll",
            26_447_980_669_367L to "7Vd9bZgt",
            455_191_289_352_668L to "25FtfZahA",
            8_269_251_997_443_309L to "bs8okw1bZ",
            91_109_839_642_700_508L to "6jHboolUPk",
            136_804_575_551_479_365L to "A6Z7GkJQov",
            9_209_662_801_739_618_667L to "AyKLuEg9idn"
        )
    }

    @Test
    fun encodeFunctionalTest() {
        val codec = PositiveNumbersBase62Codec()
        for (v in ENCODED_CONSTANTS.keys) {
            val result = codec.encode(v)
            assertEquals(ENCODED_CONSTANTS[v], result, "Encoder error encountered with value $v")
        }
    }

    @Test
    fun decodeFunctionalTest() {
        val codec = PositiveNumbersBase62Codec()
        for (e in ENCODED_CONSTANTS) {
            val result = codec.decode(e.value)
            assertEquals(e.key, result, "Decoder error encountered with value ${e.value}")
        }
    }

    @Test
    fun decodeWithLeadingZerosFunctionalTest() {
        val codec = PositiveNumbersBase62Codec()
        for (e in ENCODED_CONSTANTS) {
            val leadingZeros = Random.nextInt(1, 10)
            var valueWithZeroes = e.value
            for (i in 1..leadingZeros) {
                valueWithZeroes = "0$valueWithZeroes"
            }
            val result = codec.decode(valueWithZeroes)
            assertEquals(e.key, result, "Decoder error encountered with value $valueWithZeroes")
        }
    }

    @Test
    fun decodeEncodeFunctionalTest() {
        val codec = PositiveNumbersBase62Codec()
        for (c in 1..100) {
            val base62 = RandomStringUtils.randomAlphanumeric(9,10)
            if (base62.startsWith("0")) {
                // Pass values with leading zeros
                continue
            }
            val base10 = codec.decode(base62)
            val base62Encoded = codec.encode(base10)
            assertEquals(base62, base62Encoded, "Decoder - encoder full cycle produced wrong results with value $base62")
        }
    }

    @Test
    fun decodeValueThatOutOfRangeCausedExceptionTest() {
        assertThrows(IllegalStateException::class.java, {
            val codec = PositiveNumbersBase62Codec()
            codec.decode("AzL8n0Y58m8")
        }, "Decoder consumed value that has been out of range")
    }

    @Test
    fun decodeValueThatHasWrongLengthCausedExceptionTest() {
        assertThrows(IllegalStateException::class.java, {
            val codec = PositiveNumbersBase62Codec()
            codec.decode("AAzL8n0Y58m7")
        }, "Decoder consumed value that has wrong length")
    }

    @Test
    fun decodeEmptyValueCausedExceptionTest() {
        assertThrows(IllegalStateException::class.java, {
            val codec = PositiveNumbersBase62Codec()
            codec.decode("")
        }, "Decoder consumed empty value")
    }

    @Test
    fun encodeNegativeValueCausedExceptionTest() {
        assertThrows(IllegalStateException::class.java, {
            val codec = PositiveNumbersBase62Codec()
            codec.encode(Random.nextLong(Long.MIN_VALUE, 0))
        }, "Decoder consumed negative value")
    }

    @Test
    fun decodeValueThatHasWrongFormatCausedExceptionTest() {
        val codec = PositiveNumbersBase62Codec()
        for (c in 1..100) {
            val potentialBase62 = RandomStringUtils.randomAscii(9, 10)
            if (isAlphanumeric(potentialBase62)) {
                // Pass alphanumeric values
                continue
            }
            assertThrows(IllegalStateException::class.java, {
                codec.decode(potentialBase62)
            }, "Decoder consumed value that has wrong format")
        }
    }
}