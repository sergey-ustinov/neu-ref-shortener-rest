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
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.model.Reference
import ustinov.sergey.shortener.service.ReferenceManagerService
import ustinov.sergey.shortener.service.ValidatorService
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
        if (id.isBlank()) {
            throw WrongUserInputException("Provided link has wrong format")
        }
        val reference = referenceManagerService.getReferenceByBase62Id(id)
        httpResponse.sendRedirect(reference.url)
    }

    @PostMapping(
        consumes = [ MediaType.TEXT_PLAIN_VALUE ],
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun create(@RequestBody source: String): String {
        if (!validatorService.validateHttpReference(source)) {
            logger.info("Detected input data that didn't pass validation: $source")
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

    private fun convertToResponse(reference: Reference): String {
        return "{\"shortUrl\" : \"${cfg.getServerBasePath()}/${reference.base62Ref}\"}"
    }
}