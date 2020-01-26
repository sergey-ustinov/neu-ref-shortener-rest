package ustinov.sergey.shortener

import org.springframework.stereotype.Component
import java.util.Arrays

@Component
class PositiveNumbersBase62Codec {
    companion object {
        const val DICTIONARY_62_MAX_VALUE = "AzL8n0Y58m7"

        private val DICTIONARY_62 = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'
        )
        private val LEADING_ZEROES_REGEXP = "^0+(?!\$)".toRegex()
    }

    private var dictionary: CharArray = DICTIONARY_62

    fun encode(input: Long): String {
        check(input >= 0) { "Only non negative numbers are allowed" }

        val indicesReversal = mutableListOf<Byte>()
        val base = dictionary.size.toByte()
        extractDigit(input, base, indicesReversal)

        val sb = StringBuffer()
        for (idx in indicesReversal.reversed()) {
            sb.append(dictionary[idx.toInt()])
        }
        return sb.toString()
    }

    fun decode(input: String): Long {
        val inputEd = removeLeadingZeroes(input)
        check(!inputEd.isBlank()) { "Provided value is empty" }
        check(isBase62ValueHasProperLength(inputEd)) { "Provided value has wrong length" }
        check(validateBase62Value(inputEd)) { "Provided value has wrong encoding" }
        check(isBase62ValueIsInRange(inputEd)) { "Provided value is out of allowed range" }

        val accumulator = mutableListOf<Long>()
        var pow: Byte = 0
        for (digitChar in inputEd.reversed()) {
            val digit = convertCharToBase10(digitChar)
            val multiplier = toPositivePower(dictionary.size.toByte(), pow++)
            accumulator.add(digit * multiplier)
        }
        return accumulator.sum()
    }

    private fun removeLeadingZeroes(input: String): String {
        return LEADING_ZEROES_REGEXP.replaceFirst(input, "")
    }

    private fun validateBase62Value(input: String): Boolean {
        for (char in input) {
            if (Arrays.binarySearch(dictionary, char) < 0) {
                return false
            }
        }
        return true
    }

    private fun isBase62ValueHasProperLength(input: String): Boolean {
        return DICTIONARY_62_MAX_VALUE.length >= input.length
    }

    private fun isBase62ValueIsInRange(input: String): Boolean {
        when {
            DICTIONARY_62_MAX_VALUE.length < input.length -> return false
            DICTIONARY_62_MAX_VALUE.length > input.length -> return true
        }
        return listOf(DICTIONARY_62_MAX_VALUE, input).max() == DICTIONARY_62_MAX_VALUE
    }

    private fun extractDigit(input: Long, divider: Byte, outputIndices: MutableList<Byte>) {
        if (input < divider) {
            outputIndices.add(input.toByte())
        } else {
            val main = input / divider
            val rest = input % divider
            outputIndices.add(rest.toByte())
            extractDigit(main, divider, outputIndices)
        }
    }

    private fun convertCharToBase10(input: Char): Byte {
        for (index in dictionary.indices) {
            if (dictionary[index] == input) {
                return index.toByte()
            }
        }
        throw IllegalStateException("Wrong character: $input")
    }

    private fun toPositivePower(a: Byte, b: Byte): Long {
        check(a >= 0) { "Only non negative numbers for base value are allowed" }
        check(b >= 0) { "Only non negative numbers for power value are allowed" }
        return when (b) {
            0.toByte() -> 1
            1.toByte() -> a.toLong()
            else -> a * toPositivePower(a, (b - 1).toByte())
        }
    }
}