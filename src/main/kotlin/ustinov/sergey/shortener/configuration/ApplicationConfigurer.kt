package ustinov.sergey.shortener.configuration

import org.slf4j.LoggerFactory

open class ApplicationConfigurer(
    private val serverPort: Int,
    origins: String
) {
    companion object {
        const val API_BASE_PATH = "/api/v1"
    }

    private val logger = LoggerFactory.getLogger(ApplicationConfigurer::class.java)

    private val allowedOrigins: List<String> = origins.split(",")
    private val serverDomainNames = listOf(
        "localhost:$serverPort",
        "127.0.0.1:$serverPort"
    )
    private val serverURLs = serverDomainNames.map {
        listOf("http://$it", it)
    }.flatten()

    fun getPort() = serverPort

    fun getServerBasePath() = "http://localhost:$serverPort$API_BASE_PATH"

    fun getServerDomainNames() = serverDomainNames

    open fun getServerURLs(): List<String> = serverURLs

    fun getAllowedOrigins() = allowedOrigins

    fun printApplicationVariables(): ApplicationConfigurer {
        logger.info("""


            ========================== App Info ==========================
            Server port is: $serverPort
            Server base path is: ${getServerBasePath()}
            Server domain names are: ${getServerDomainNames()}
            Server URLs are: ${getServerURLs()}
            Allowed origins are: ${getAllowedOrigins()}
            ==============================================================

        """.trimIndent()
        )
        return this
    }
}