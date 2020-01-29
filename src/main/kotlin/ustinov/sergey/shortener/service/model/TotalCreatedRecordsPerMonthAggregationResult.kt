package ustinov.sergey.shortener.service.model

data class TotalCreatedRecordsPerMonthAggregationResult(
    val year: Int,
    val month: Int,
    val total: Int
)