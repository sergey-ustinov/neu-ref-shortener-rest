package ustinov.sergey.shortener.service

import org.joda.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.service.model.CreatedRecordsPerSecondAggregationResult

@Component
class ManagementFacade {

    @Autowired
    private lateinit var statisticRepository: StatisticRepository

    companion object {
        private val EMPTY_RESULT = CreatedRecordsPerSecondAggregationResult(0,0,0.0,0)
        private val VALID_DATE_REGEXP =
            "^(?:19|20)\\d{2}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])\$".toRegex()
    }

    fun getCreatedRecordsPerSecondStatistic(input: String?): CreatedRecordsPerSecondAggregationResult {
        if (input.isNullOrBlank()) {
            throw WrongUserInputException("Required query parameter 'date' wasn't provided. Date must have format YYYY-MM-DD")
        }
        if (!isDateHasValidFormat(input)) {
            throw WrongUserInputException("Query parameter 'date' has an invalid format. Date must have format YYYY-MM-DD")
        }
        val date = try {
            LocalDate.parse(input)
        } catch (e: Exception) {
            throw WrongUserInputException("Invalid date value: $input. Date must have format YYYY-MM-DD")
        }
        if (isDateInTheFuture(date)) {
            throw WrongUserInputException("Unable to calculate statistic for date: $input.")
        }
        return statisticRepository.getCreatedRecordsPerSecondStatistic(date, date.plusDays(1)) ?:
            EMPTY_RESULT
    }

    private fun isDateInTheFuture(date: LocalDate): Boolean {
        return LocalDate.now().compareTo(date) == -1
    }

    private fun isDateHasValidFormat(input: String): Boolean {
        return VALID_DATE_REGEXP.find(input) != null
    }
}