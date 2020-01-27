package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@SpringBootApplication(
    exclude = [
        MongoAutoConfiguration::class,
        MongoDataAutoConfiguration::class
    ]
)
@EnableConfigurationProperties(MongoConfig::class)
open class UrlShortenerApp {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(UrlShortenerApp::class.java)
                .bannerMode(Banner.Mode.OFF)
                .run()
        }
    }

    @Value("\${allowed-origins}")
    private lateinit var allowedOrigins: String

    @Bean(name = [ "AllowedOrigins" ])
    open fun allowedOrigins() = allowedOrigins
}