package ustinov.sergey.shortener.model

import org.joda.time.LocalDateTime

data class Reference(
    val base10Ref: Long,
    val base62Ref: String,
    val url: String,
    val created: LocalDateTime = LocalDateTime.now()
)