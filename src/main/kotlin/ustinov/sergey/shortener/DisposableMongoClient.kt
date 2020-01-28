package ustinov.sergey.shortener

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.slf4j.LoggerFactory

class DisposableMongoClient(uri: MongoClientURI) : MongoClient(uri), Disposable {
    private val logger = LoggerFactory.getLogger(DisposableMongoClient::class.java)

    override fun dispose() {
        logger.info("Freeing resources")
        close()
    }
}