package ustinov.sergey.shortener.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mongodb")
class MongoConfig {
    var url: String? = null
    var db: String? = null
    var password: String? = null
    var user: String? = null

    var connectTimeout: Int? = null
    var socketTimeout: Int? = null
    var maxConnectionIdleTime: Int? = null
    var retryWrites: Boolean? = null
    var minConnectionsPerHost: Int? = null
    var maxConnectionsPerHost: Int? = null
    var writeConcern: WriteConcern? = null

}

class WriteConcern {
    var w: String? = null
    var wTimeoutMS: Long? = null
    var journal: Boolean? = null
}