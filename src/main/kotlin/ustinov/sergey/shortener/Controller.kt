package ustinov.sergey.shortener

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ustinov.sergey.shortener.configuration.ApplicationConfigurer
import ustinov.sergey.shortener.configuration.ApplicationConfigurer.Companion.API_BASE_PATH
import ustinov.sergey.shortener.exceptions.AbstractSystemException
import ustinov.sergey.shortener.exceptions.ServerException
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.model.Reference
import ustinov.sergey.shortener.service.ReferenceManagerService
import ustinov.sergey.shortener.service.ValidatorService
import ustinov.sergey.shortener.service.ValidatorService.Companion.MAX_REFERENCE_COUNT_IN_BATCH
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(API_BASE_PATH)
class Controller {

    @Autowired
    private lateinit var referenceManagerService: ReferenceManagerService
    @Autowired
    private lateinit var validatorService: ValidatorService
    @Autowired
    private lateinit var cfg: ApplicationConfigurer

    private val logger = LoggerFactory.getLogger(Controller::class.java)

    @GetMapping("/{id}",
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun resolve(
        @PathVariable id: String,
        httpResponse: HttpServletResponse
    ) {
        try {
            if (id.isBlank()) {
                throw WrongUserInputException("Provided link has wrong format")
            }
            val reference = referenceManagerService.getReferenceByBase62Id(id)
            httpResponse.sendRedirect(reference.url)
        } catch (e: AbstractSystemException) {
            throw e
        } catch (e: Exception) {
            logger.error("Server exception encountered", e)
            throw ServerException("Some error occurred")
        }
    }

    @PostMapping("/bulk",
        consumes = [ MediaType.TEXT_PLAIN_VALUE ],
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun createBulk(@RequestBody source: String): String {
        try {
            val inputData = source.split('\n')
            val blankData = inputData.filter { it.isBlank() }
            val invalidData = mutableMapOf<Int, String>()

            if (blankData.isNotEmpty()) {
                throw WrongUserInputException("Provided list of references has empty strings. Unable to shorten this")
            }
            if (inputData.size > MAX_REFERENCE_COUNT_IN_BATCH) {
                logger.info("Detected bulk of input data with size ${inputData.size}")
                throw WrongUserInputException("Provided list of references is too big. Max size for the list is $MAX_REFERENCE_COUNT_IN_BATCH. Unable to shorten this")
            }
            inputData.forEachIndexed { index, input ->
                if (!validatorService.validateHttpReference(input)) {
                    logger.info("Detected input data that didn't pass validation: '$input'")
                    invalidData[index] = input
                } else if (!validatorService.isDomainAllowed(input)) {
                    logger.info("Detected input data of restricted domain: $input")
                    invalidData[index] = input
                }
            }
            if (invalidData.isNotEmpty()) {
                throw WrongUserInputException("Provided list of references has invalid links at positions: ${convertToValidationResponse(invalidData)}. Unable to shorten this")
            }
            return convertToResponse(
                referenceManagerService.createNewReferences(inputData)
            )
        } catch (e: AbstractSystemException) {
            throw e
        } catch (e: Exception) {
            logger.error("Server exception encountered", e)
            throw ServerException("Some error occurred")
        }
    }

    @PostMapping(
        consumes = [ MediaType.TEXT_PLAIN_VALUE ],
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun create(@RequestBody source: String): String {
        try {
            if (!validatorService.validateHttpReference(source)) {
                logger.info("Detected input data that didn't pass validation: '$source'")
                throw WrongUserInputException("Provided reference has wrong format. Unable to shorten this")
            }
            if (!validatorService.isDomainAllowed(source)) {
                logger.info("Detected input data of restricted domain: $source")
                throw WrongUserInputException("Provided reference can't be shortened")
            }
            return convertToResponse(
                referenceManagerService.createNewReference(source)
            )
        } catch (e: AbstractSystemException) {
            throw e
        } catch (e: Exception) {
            logger.error("Server exception encountered", e)
            throw ServerException("Some error occurred")
        }
    }

    private fun convertToValidationResponse(input: Map<Int, String>)
        = input.keys.map { it + 1 }.toSortedSet().joinToString()


    private fun convertToResponse(reference: List<Reference>): String {
        val preparedLinks = reference.map {
            "\t\"${cfg.getServerBasePath()}/${it.base62Ref}\""
        }
        val referencesJSONArray = preparedLinks.joinToString(
            separator = ",\n", prefix = "[\n", postfix = "\n]"
        )
        return "{\"shortUrl\" : $referencesJSONArray}"
    }

    private fun convertToResponse(reference: Reference): String {
        return "{\"shortUrl\" : \"${cfg.getServerBasePath()}/${reference.base62Ref}\"}"
    }
}