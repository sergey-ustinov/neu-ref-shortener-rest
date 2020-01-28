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
import ustinov.sergey.shortener.configuration.ApplicationConfigurer.Companion.API_BASE_PATH
import ustinov.sergey.shortener.exceptions.AbstractSystemException
import ustinov.sergey.shortener.exceptions.ServerException
import ustinov.sergey.shortener.service.ReceivingFacade
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(API_BASE_PATH)
class Controller {

    @Autowired
    private lateinit var receivingFacade: ReceivingFacade

    private val logger = LoggerFactory.getLogger(Controller::class.java)

    @GetMapping("/{id}",
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun resolve(
        @PathVariable id: String,
        httpResponse: HttpServletResponse
    ) {
        safeExecute {
            httpResponse.sendRedirect(
                receivingFacade.resolve(id).url
            )
        }
    }

    @PostMapping("/bulk",
        consumes = [ MediaType.TEXT_PLAIN_VALUE ],
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun createBulk(@RequestBody source: String): String {
        return safeExecute {
            receivingFacade.createBulk(source)
        }
    }

    @PostMapping(
        consumes = [ MediaType.TEXT_PLAIN_VALUE ],
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun create(@RequestBody source: String): String {
        return safeExecute {
            receivingFacade.create(source)
        }
    }

    private fun <T> safeExecute(action: () -> T): T {
        try {
            return action()
        } catch (e: AbstractSystemException) {
            throw e
        } catch (e: Exception) {
            logger.error("Server exception encountered", e)
            throw ServerException("Some error occurred")
        }
    }
}