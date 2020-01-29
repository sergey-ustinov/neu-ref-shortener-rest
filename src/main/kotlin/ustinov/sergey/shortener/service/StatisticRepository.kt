package ustinov.sergey.shortener.service

import org.joda.time.LocalDate
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import ustinov.sergey.shortener.model.Reference
import ustinov.sergey.shortener.service.model.CreatedRecordsPerSecondAggregationResult
import ustinov.sergey.shortener.service.model.TotalCreatedRecordsPerMonthAggregationResult

interface StatisticRepository : MongoRepository<Reference, String> {

    @Aggregation( pipeline = [
        "{ '\$match' : { 'created' : { '\$gte' : ?0, '\$lt' : ?1 } } }",
        "{ '\$group' : { '_id' : { 'year' : { '\$year' : '\$created' }, 'dayOfYear' : { '\$dayOfYear' : '\$created' }, 'hour' : { '\$hour' : '\$created' }, 'minute' : { '\$minute' : '\$created' }, 'second' : { '\$second' : '\$created' } }, 'count' : { '\$sum': { '\$const' : NumberInt(1) } } } }",
        "{ '\$group' : { '_id' : { '\$const' : NumberInt(1) }, 'min' : { '\$min' : '\$count' }, 'max' : { '\$max' : '\$count' }, 'avg' : { '\$avg' : '\$count' }, 'total' : { '\$sum' : '\$count' } } }"
    ])
    fun getCreatedRecordsPerSecondStatistic(from: LocalDate, to: LocalDate): CreatedRecordsPerSecondAggregationResult?

    @Aggregation( pipeline = [
        "{ '\$match' : { 'created' : { '\$gte' : ?0, '\$lt' : ?1 } } }",
        "{ '\$group' : { '_id' : { 'year' : { '\$year' : '\$created' }, 'month' : { '\$month' : '\$created' } }, 'total' : { '\$sum': { '\$const' : NumberInt(1) } } } }",
        "{ '\$project' : { 'year': '\$_id.year', 'month' : '\$_id.month', 'total' : '\$total' } }"
    ])
    fun getTotalCreatedRecordsPerMonthStatistic(from: LocalDate, to: LocalDate): List<TotalCreatedRecordsPerMonthAggregationResult>
}