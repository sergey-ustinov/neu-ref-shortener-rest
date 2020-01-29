package ustinov.sergey.shortener.service.model

data class CreatedRecordsPerSecondAggregationResult(
    val min: Int,
    val max: Int,
    val avg : Double,
    val total: Int
)