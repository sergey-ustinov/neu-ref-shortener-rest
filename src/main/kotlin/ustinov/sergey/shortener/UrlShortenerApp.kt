package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import ustinov.sergey.shortener.auxiliary.Disposable
import ustinov.sergey.shortener.auxiliary.ExampleServletContextListener
import ustinov.sergey.shortener.auxiliary.JavaSystemVariablesPrinter.printJavaVariables
import ustinov.sergey.shortener.configuration.ApplicationConfigurer
import ustinov.sergey.shortener.configuration.MongoConfig
import javax.servlet.ServletContextListener

@SpringBootApplication(
    exclude = [
        MongoAutoConfiguration::class,
        MongoDataAutoConfiguration::class
    ]
)
@EnableConfigurationProperties(MongoConfig::class)
@EnableMongoRepositories
open class UrlShortenerApp {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(UrlShortenerApp::class.java)
                .bannerMode(Banner.Mode.OFF)
                .run()
            printJavaVariables()
        }
    }

    @Bean
    open fun applicationConfigurer(
        @Value("\${server.port}") port: Int,
        @Value("\${allowed-origins}") origins: String
    ) = ApplicationConfigurer(port, origins).printApplicationVariables()

    @Bean
    open fun servletListener(disposableBeans: List<Disposable>)
        = ServletListenerRegistrationBean<ServletContextListener>(ExampleServletContextListener(disposableBeans))
}