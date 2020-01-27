package ustinov.sergey.shortener

import org.springframework.stereotype.Component
import java.util.Objects.isNull

/**
 * Performs basic validations on source URL reference
 *
 * 1). Privided link must be not blank
 * 2). Provided link must be not greater than 2048 characters
 * 3). It shouldn't contains whitespace characters
 * 4). It shouldn't lead to any resource on the service's domain
 * 5). It can contain one of the following prefixes : http:// or https:// (data:, ftp:// and file:// is denied)
 * 6). If provided link starts from sequence of digits formatted like IP address,
 *     then this sequence of digits should satisfy the IP address regexp pattern
 *     (it doesn't matter in this case, has the provided link protocol prefix or not)
 */
@Component
class ValidatorService(
    private val cfg: ApplicationConfigurer
) {

    companion object {
        const val MAX_REFERENCE_LENGTH = 2048

        private val VALID_IP_V4_ADDRESS_REGEXP = (
            "^(?:(?:[0-9]{1,2}|0[0-9]{1,2}|1[0-9]{1,2}|2[0-5]{2})(?:\\.)){3}" +
            "(?:(?:[0-9]{1,2}|0[0-9]{1,2}|1[0-9]{1,2}|2[0-5]{2})(?=\$|\\/|:|\\?))"
        ).toRegex()

        private val POSSIBLE_IP_V4_ADDRESS_REGEXP =
            "(?:(?:[0-9]{1,3})(?:\\.)){3}(?:(?:[0-9]{1,3})(?=\$|\\/|:|\\?))"
            .toRegex()
    }

    enum class Protocol(
        private val prefix: String,
        private val allowed: Boolean
    ){
        HTTP("http://", true),
        HTTPS("https://", true),
        FTP("ftp://", false),
        FILE("file://", false),
        DATA("data:", false);

        fun getPrefix() = prefix
        fun isAllowed() = allowed

        companion object {
            val ALLOWED_PROTOCOLS = values().filter { it.isAllowed() }
            val NOT_ALLOWED_PROTOCOLS = values().filter { !it.isAllowed() }
        }
    }

    fun isDomainAllowed(input: String): Boolean {
        for (url in cfg.getServerURLs()) {
            if (input.startsWith(url, true)) {
                return false
            }
        }
        return true
    }

    fun validateHttpReference(input: String): Boolean {
        val protocol = defineProtocol(input)
        val inputEd = trimProtocolPrefix(input, protocol)

        return input.isNotBlank() &&
                !isGreaterThanMaxLength(input) &&
                 !hasWhitespace(inputEd) &&
                  (isNull(protocol) || protocol!!.isAllowed()) &&
                   (!hasIPAddressMask(inputEd) || hasValidIPAddress(inputEd))
    }

    private fun hasIPAddressMask(input: String): Boolean {
        return POSSIBLE_IP_V4_ADDRESS_REGEXP.find(input) != null
    }

    private fun hasValidIPAddress(input: String): Boolean {
        return VALID_IP_V4_ADDRESS_REGEXP.find(input) != null
    }

    private fun trimProtocolPrefix(input: String, protocol: Protocol?): String {
        return if (protocol != null) {
            input.replaceFirst(protocol.getPrefix(), "", true)
        } else {
            input
        }
    }

    private fun defineProtocol(input: String): Protocol? {
        for (protocol in Protocol.values()) {
            if (input.startsWith(protocol.getPrefix(), true)) {
                return protocol
            }
        }
        return null
    }

    private fun isGreaterThanMaxLength(input: String): Boolean {
        return input.length > MAX_REFERENCE_LENGTH
    }

    private fun hasWhitespace(input: String): Boolean {
        for (c in input) {
            if (c.isWhitespace()) {
                return true
            }
        }
        return false
    }
}