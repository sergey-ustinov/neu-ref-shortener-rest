package ustinov.sergey.shortener

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import ustinov.sergey.shortener.service.ValidatorService.Companion.MAX_REFERENCE_LENGTH
import ustinov.sergey.shortener.Protocol.Companion.ALLOWED_PROTOCOLS
import ustinov.sergey.shortener.Protocol.Companion.NOT_ALLOWED_PROTOCOLS
import ustinov.sergey.shortener.configuration.ApplicationConfigurer
import ustinov.sergey.shortener.service.ValidatorService
import kotlin.random.Random

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidatorServiceTest {

    companion object {
        val whitespaceCharacters = mapOf(
            "Horizontal Tabulation" to '\u0009',
            "Line Feed" to '\u000A',
            "Vertical Tabulation" to '\u000B',
            "From Feed" to '\u000C',
            "Carriage Return" to '\u000D',
            "File Separator" to '\u001C',
            "Group Separator" to '\u001D',
            "Record Separator" to '\u001E',
            "Unit Separator" to '\u001F',
            "No-Break Space" to '\u00A0',
            "Figure Space" to '\u2007',
            "Narrow No-Break Space" to '\u202F',
            "Paragraph Separator" to '\u2029',
            "Line Separator" to '\u2028',
            "Space" to '\u0020',
            "No-Break Space" to '\u00A0',
            "Ogham Space Mark" to '\u1680',
            "En Quad" to '\u2000',
            "Em Quad" to '\u2001',
            "En Space" to '\u2002',
            "Em Space" to '\u2003',
            "Three-Per-Em Space" to '\u2004',
            "Four-Per-Em Space" to '\u2005',
            "Six-Per-Em Space" to '\u2006',
            "Figure Space" to '\u2007',
            "Punctuation Space" to '\u2008',
            "Thin Space" to '\u2009',
            "Hair Space" to '\u200A',
            "Narrow No-Break Space" to '\u202F',
            "Medium Mathematical Space" to '\u205F',
            "Ideographic Space" to '\u3000'
        )
    }

    private lateinit var cfg: ApplicationConfigurer
    private lateinit var service: ValidatorService

    @BeforeEach
    private fun init(@Mock applicationConfigurer: ApplicationConfigurer) {
        service = ValidatorService(applicationConfigurer)
        cfg = applicationConfigurer
    }

    @Test
    fun urlReferenceMustBeNotEmptyTest() {
        val result = service.validateHttpReference(
            ""
        )
        assertFalse(result) { "Validator missed empty input" }
    }

    @Test
    fun urlReferenceMustBeNotBlankTest() {
        val sb = StringBuffer()
        whitespaceCharacters.values.forEach {
            sb.append(it)
        }
        val result = service.validateHttpReference(
            sb.toString()
        )
        assertFalse(result) { "Validator missed blank input" }
    }

    @Test
    fun urlReferenceMustBeLessThanMaxReferenceLengthTest() {
        val sb = StringBuffer()
        for (i in 1..MAX_REFERENCE_LENGTH + 1) {
            sb.append(".")
        }
        val result = service.validateHttpReference(
            sb.toString()
        )
        assertFalse(result) { "Validator missed input that greater than MAX_REFERENCE_LENGTH=$MAX_REFERENCE_LENGTH" }
    }

    @Test
    fun urlReferenceShouldntContainsWhitespaceCharactersTest() {
        val index = Random.nextInt(0, whitespaceCharacters.size)
        val whitespace = whitespaceCharacters.values.toMutableList()[index]
        val result = service.validateHttpReference(
            "http://some-resource.com/path?id=1${whitespace}0"
        )
        assertFalse(result) { "Validator missed input with whitespace character" }
    }

    @Test
    fun urlReferenceShouldntLeadToServiceDomainTest() {
        val restrictedDomain = "restricted-domain.com"
        `when`(cfg.getServerURLs()).thenAnswer {
            listOf(
                "httP://$restrictedDomain",
                restrictedDomain
            )
        }

        for (p in listOf(Protocol.HTTP, null)) {
            val validProtocol = p?.getPrefix()?.toLowerCase() ?: ""
            val result = service.isDomainAllowed(
                "$validProtocol$restrictedDomain/path-to-resource?id=1112"
            )
            assertFalse(result) { "Validator failed input with restricted domain value" }
        }
    }

    @Test
    fun urlReferenceCanHaveAllowedProtocolPrefixTest() {
        for (p in ALLOWED_PROTOCOLS) {
            val validProtocol = p.getPrefix().toUpperCase()
            val result = service.validateHttpReference(
                "${validProtocol}some-resource.com/path"
            )
            assertTrue(result) { "Validator failed input with valid protocol: $validProtocol" }
        }
    }

    @Test
    fun urlReferenceCantHaveNotAllowedProtocolPrefixTest() {
        for (p in NOT_ALLOWED_PROTOCOLS) {
            val invalidProtocol = p.getPrefix().toUpperCase()
            val result = service.validateHttpReference(
                "${invalidProtocol}some-resource.com/path"
            )
            assertFalse(result) { "Validator missed input with invalid protocol: $invalidProtocol" }
        }
    }

    @Test
    fun urlReferenceCanBeWithoutPrefixTest() {
        val result = service.validateHttpReference(
            "www.some-resource.com/path?id=15986"
        )
        assertTrue(result) { "Validator missed input without protocol value" }
    }

    @Test
    fun urlReferenceCanContainOnlyValidIPv4AddressesTest() {
        val index = Random.nextInt(0, ALLOWED_PROTOCOLS.size)
        val input = """
            ${ALLOWED_PROTOCOLS[index].getPrefix()}
            ${Random.nextInt(0, 256)}.
            ${Random.nextInt(0, 256)}.
            ${Random.nextInt(0, 256)}.
            ${Random.nextInt(0, 256)}
            /path?id=15986
        """.trimIndent().replace("\n", "")
        val result = service.validateHttpReference(input)
        assertTrue(result) { "Validator missed input with valid IPv4 address: $input" }
    }

    @Test
    fun urlReferenceCantContainInvalidIPv4AddressesTest() {
        val index = Random.nextInt(0, ALLOWED_PROTOCOLS.size)
        val input = """
            ${ALLOWED_PROTOCOLS[index].getPrefix()}
            ${Random.nextInt(256, 999)}.
            ${Random.nextInt(0, 256)}.
            ${Random.nextInt(0, 256)}.
            ${Random.nextInt(0, 256)}
            /path?id=15986
        """.trimIndent().replace("\n", "")
        val result = service.validateHttpReference(input)
        assertFalse(result) { "Validator missed input with invalid IPv4 address: $input" }
    }

    @Test
    fun allOfTheseURLsAreInvalidTest() {
        val inputData = listOf(
            "http://123.22.90.277:8090/path-to-resource/123.22.90.27",
            "http://123.22.90.277:8090/path-to-resource",
            "SELECT * FROM USERS;",
            "ftp://user:password@165.23.2.88/path-to-files",
            "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ%3D%3D",
            "file:///Users/admin/Documents"
        )
        for (input in inputData) {
            val result = service.validateHttpReference(input)
            assertFalse(result) { "Validator missed input with invalid URL address: $input" }
        }
    }
}