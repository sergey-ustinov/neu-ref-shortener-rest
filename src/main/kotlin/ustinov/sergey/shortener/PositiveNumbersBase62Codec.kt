package ustinov.sergey.shortener

import org.springframework.stereotype.Component
import java.util.Arrays

@Component
class PositiveNumbersBase62Codec {
    companion object {
        val DICTIONARY_62 = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'
        )
        const val DICTIONARY_62_MAX_VALUE = "AzL8n0Y58m7"
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
        check(validateBase62Value(input)) { "Provided value has wrong encoding" }
        check(isBase62ValueIsInRange(input)) { "Provided value is out of allowed range" }

        val accumulator = mutableListOf<Long>()
        var pow: Byte = 0
        for (digitChar in input.reversed()) {
            val digit = convertCharToBase10(digitChar)
            val multiplier = toPositivePower(dictionary.size.toByte(), pow++)
            accumulator.add(digit * multiplier)
        }
        return accumulator.sum()
    }

    private fun validateBase62Value(input: String): Boolean {
        for (char in input) {
            if (Arrays.binarySearch(dictionary, char) < 0) {
                return false
            }
        }
        return true
    }

    private fun isBase62ValueIsInRange(input: String): Boolean {
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