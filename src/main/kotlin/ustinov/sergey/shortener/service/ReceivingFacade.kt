package ustinov.sergey.shortener.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import ustinov.sergey.shortener.configuration.ApplicationConfigurer
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.model.Reference
import ustinov.sergey.shortener.service.ValidatorService.Companion.MAX_REFERENCE_COUNT_IN_BATCH

@Component
class ReceivingFacade {

    @Autowired
    private lateinit var referenceManagerService: ReferenceManagerService
    @Autowired
    private lateinit var validatorService: ValidatorService
    @Autowired
    private lateinit var cfg: ApplicationConfigurer

    private val logger = LoggerFactory.getLogger(ReceivingFacade::class.java)

    fun resolve(id: String): Reference {
        if (id.isBlank()) {
            throw WrongUserInputException("Provided link has wrong format")
        }
        return referenceManagerService.getReferenceByBase62Id(id)
    }

    fun createBulk(source: String): String {
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
    }

    fun create(@RequestBody source: String): String {
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