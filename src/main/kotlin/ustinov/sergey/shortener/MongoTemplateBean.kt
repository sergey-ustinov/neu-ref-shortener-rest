package ustinov.sergey.shortener

import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.MongoClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import com.mongodb.WriteConcern
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import java.util.concurrent.TimeUnit

@Configuration
open class MongoTemplateFactoryBean {

    @Autowired
    private lateinit var config: MongoConfig

    @Bean
    open fun createMongoTemplate(): MongoTemplate {
        val mongoDbFactory = SimpleMongoDbFactory(mongoClient(), config.db!!)
        return MongoTemplate(mongoDbFactory, getConverter(mongoDbFactory))
    }

    private fun mongoClient(): MongoClient {
        val optsBuilder = MongoClientOptions.Builder()
            .connectionsPerHost(config.maxConnectionsPerHost!!)
            .minConnectionsPerHost(config.minConnectionsPerHost!!)
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.MAJORITY)
            .retryWrites(config.retryWrites!!)
            .connectTimeout(config.connectTimeout!!)
            .socketTimeout(config.socketTimeout!!)
            .maxConnectionIdleTime(config.maxConnectionIdleTime!!)

        config.writeConcern?.let { optsBuilder.writeConcern(WriteConcern(it.w!!).withWTimeout(it.wTimeoutMS!!, TimeUnit.MILLISECONDS).withJournal(it.journal!!)) }

        // TODO : perform close here
        return MongoClient(MongoClientURI(config.url!!, optsBuilder))
    }

    private fun getConverter(mongoDbFactory: SimpleMongoDbFactory): MappingMongoConverter {
        val converter = MappingMongoConverter(DefaultDbRefResolver(mongoDbFactory), MongoMappingContext())
        converter.typeMapper = DefaultMongoTypeMapper(null)
        converter.afterPropertiesSet()
        return converter
    }
}