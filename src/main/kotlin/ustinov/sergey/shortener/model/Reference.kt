package ustinov.sergey.shortener.model

data class Reference(
    val base10Ref: Long,
    val base62Ref: String,
    val url: String
)