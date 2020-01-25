package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.model.Reference
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class Controller {
    @Autowired
    private lateinit var referenceManagerService: ReferenceManagerService
    @Autowired
    private lateinit var validatorService: ValidatorService

    @GetMapping("/{id}")
    fun resolve(
        @PathVariable id: String,
        httpRequest: HttpServletRequest,
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
            throw WrongUserInputException("Provided reference has wrong format. Unable to shorten this")
        }
        if (validatorService.isReferenceAlreadyShorten(source)) {
            throw WrongUserInputException("Provided reference is shorten already")
        }
        return convertToResponse(
            referenceManagerService.createNewReference(source)
        )
    }

    private fun convertToResponse(reference: Reference): String {
        return "{\"short_url\" : \"localhost:8080/${reference.base62Ref}\"}"
    }
}