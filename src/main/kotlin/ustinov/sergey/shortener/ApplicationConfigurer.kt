package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ApplicationConfigurer {
    companion object {
        const val API_BASE_PATH = "/api/v1"
    }

    @Value("\${server.port}")
    private lateinit var port: String
    @Value("\${allowed-origins}")
    private lateinit var allowedOrigins: String

    private lateinit var serverDomainNames: List<String>
    private lateinit var serverURLs: List<String>

    @PostConstruct
    fun init() {
        serverDomainNames = listOf(
            "localhost:$port", "127.0.0.1:$port"
        )
        serverURLs = serverDomainNames.map {
            listOf("http://$it", "https://$it", it)
        }.flatten()
    }

    fun getPort() = port

    fun getServerBasePath() = "http://localhost:$port$API_BASE_PATH"

    fun getServerDomainNames() = serverDomainNames

    fun getServerURLs() = serverURLs

    fun getAllowedOrigins(): List<String> = allowedOrigins.split(",")
}