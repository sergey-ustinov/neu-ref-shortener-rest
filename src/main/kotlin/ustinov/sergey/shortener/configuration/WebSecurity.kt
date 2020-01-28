package ustinov.sergey.shortener.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ustinov.sergey.shortener.configuration.ApplicationConfigurer.Companion.API_BASE_PATH

@Configuration
@EnableWebSecurity
open class WebSecurity : WebSecurityConfigurerAdapter() {
    @Bean
    open fun corsConfigurer(cfg: ApplicationConfigurer): WebMvcConfigurer {
        val allowedOrigins = cfg.getAllowedOrigins()
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
            .antMatchers("$API_BASE_PATH/**")
            .antMatchers("/error")
    }
}