package ustinov.sergey.shortener.service.model

enum class Protocol(
    private val prefix: String,
    private val allowed: Boolean
){
    HTTP("http://", true),
    HTTPS("https://", true),
    FTP("ftp://", false),
    FILE("file://", false),
    DATA("data:", false);

    fun getPrefix() = prefix
    fun isAllowed() = allowed

    companion object {
        val ALLOWED_PROTOCOLS = values().filter { it.isAllowed() }
        val NOT_ALLOWED_PROTOCOLS = values().filter { !it.isAllowed() }
    }
}