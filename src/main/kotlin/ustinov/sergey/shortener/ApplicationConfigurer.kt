package ustinov.sergey.shortener

open class ApplicationConfigurer(
    private val serverPort: Int,
    origins: String
) {
    companion object {
        const val API_BASE_PATH = "/api/v1"
    }

    private val allowedOrigins: List<String> = origins.split(",")
    private val serverDomainNames = listOf(
        "localhost:$serverPort",
        "127.0.0.1:$serverPort"
    )
    private val serverURLs = serverDomainNames.map {
        listOf("http://$it", "https://$it", it)
    }.flatten()

    fun getPort() = serverPort

    fun getServerBasePath() = "http://localhost:$serverPort$API_BASE_PATH"

    fun getServerDomainNames() = serverDomainNames

    open fun getServerURLs(): List<String> = serverURLs

    fun getAllowedOrigins() = allowedOrigins
}