package ustinov.sergey.shortener.configuration

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import ustinov.sergey.shortener.DisposableMongoClient
import java.util.concurrent.TimeUnit.MILLISECONDS

@Configuration
open class MongoTemplateFactoryBean {

    @Autowired
    private lateinit var config: MongoConfig

    @Bean
    open fun createMongoTemplate(mongoClient: DisposableMongoClient): MongoTemplate {
        val mongoDbFactory = SimpleMongoDbFactory(mongoClient, config.db!!)
        return MongoTemplate(mongoDbFactory, getConverter(mongoDbFactory))
    }

    @Bean
    open fun mongoClient(): DisposableMongoClient {
        val optsBuilder = MongoClientOptions.Builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.MAJORITY)
            .retryWrites(config.retryWrites!!)
            .withConnectionSettings()
            .withWriteConcern()
        return DisposableMongoClient(MongoClientURI(config.url!!, optsBuilder))
    }

    private fun getConverter(mongoDbFactory: SimpleMongoDbFactory): MappingMongoConverter {
        val converter = MappingMongoConverter(DefaultDbRefResolver(mongoDbFactory), MongoMappingContext())
        converter.typeMapper = DefaultMongoTypeMapper(null)
        converter.afterPropertiesSet()
        return converter
    }

    private fun MongoClientOptions.Builder.withConnectionSettings(): MongoClientOptions.Builder {
        connectionsPerHost(config.maxConnectionsPerHost!!)
            .minConnectionsPerHost(config.minConnectionsPerHost!!)
            .connectTimeout(config.connectTimeout!!)
            .socketTimeout(config.socketTimeout!!)
            .maxConnectionIdleTime(config.maxConnectionIdleTime!!)
        return this
    }

    private fun MongoClientOptions.Builder.withWriteConcern(): MongoClientOptions.Builder {
        config.writeConcern?.apply {
            writeConcern(
                WriteConcern(w!!)
                    .withWTimeout(wTimeoutMS!!, MILLISECONDS)
                    .withJournal(journal!!)
            )
        }
        return this
    }
}