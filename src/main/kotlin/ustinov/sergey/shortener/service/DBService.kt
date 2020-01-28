package ustinov.sergey.shortener.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import ustinov.sergey.shortener.model.Reference
import ustinov.sergey.shortener.model.Sequence

@Component
class DBService  {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun getByBase62Id(base62: String): Reference? {
        return mongoTemplate.find(
            query(where("base62Ref").`is`(base62)), Reference::class.java
        ).firstOrNull()
    }

    fun getByBase10Id(base10: Long): Reference? {
        return mongoTemplate.find(
            query(where("base10Ref").`is`(base10)), Reference::class.java
        ).firstOrNull()
    }

    fun save(reference: Reference): Reference {
        return mongoTemplate.insert(reference)
    }

    fun getNextSequenceValue(): Long {
        val options = FindAndModifyOptions.options()
            .returnNew(true)
            .upsert(true)

        val sequence = mongoTemplate.findAndModify(
            query(where("name").`is`("default")),
            Update().inc("value", 1),
            options,
            Sequence::class.java
        )
        return sequence!!.value
    }
}