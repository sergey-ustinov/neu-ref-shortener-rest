package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
open class WebSecurity : WebSecurityConfigurerAdapter() {

    private val apiPath = "/api/v1"

    @Bean
    open fun corsConfigurer(@Qualifier("AllowedOrigins") origins: String): WebMvcConfigurer {
        val allowedOrigins = origins.split(",")

        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins(*allowedOrigins.toTypedArray())
                    .allowedMethods("GET", "POST")
                    .allowedHeaders("*")
            }
        }
    }

    override fun configure(web: WebSecurity) {
        web.ignoring()
            .antMatchers(
                HttpMethod.GET,
                "favicon.ico"
            )
            .antMatchers("$apiPath/**")
            .antMatchers("/error")
    }
}