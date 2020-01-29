package ustinov.sergey.shortener

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ustinov.sergey.shortener.configuration.ApplicationConfigurer.Companion.MGMT_BASE_PATH
import ustinov.sergey.shortener.exceptions.AbstractSystemException
import ustinov.sergey.shortener.exceptions.ServerException
import ustinov.sergey.shortener.service.ManagementFacade
import ustinov.sergey.shortener.service.model.CreatedRecordsPerSecondAggregationResult
import ustinov.sergey.shortener.service.model.TotalCreatedRecordsPerMonthAggregationResult

@RestController
@RequestMapping(MGMT_BASE_PATH)
class ManagementController {

    private val logger = LoggerFactory.getLogger(ManagementController::class.java)

    @Autowired
    private lateinit var managementFacade: ManagementFacade

    @GetMapping("/year",
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun getTotalCreatedRecordsPerMonthStatistic(
        @RequestParam("year", required = false) input: String?
    ): List<TotalCreatedRecordsPerMonthAggregationResult> {
        return safeExecute {
            managementFacade.getTotalCreatedRecordsPerMonthStatistic(input)
        }
    }

    @GetMapping("/creation",
        produces = [ MediaType.APPLICATION_JSON_VALUE ]
    )
    fun getCreatedRecordsPerSecondStatistic(
        @RequestParam("date", required = false) input: String?
    ): CreatedRecordsPerSecondAggregationResult {
        return safeExecute {
            managementFacade.getCreatedRecordsPerSecondStatistic(input)
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