package ustinov.sergey.shortener.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ustinov.sergey.shortener.exceptions.ReferenceNotFoundException
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.model.Reference
import java.util.Objects.isNull

@Component
class ReferenceManagerService {
    @Autowired
    private lateinit var dbService: DBService
    @Autowired
    private lateinit var positiveNumbersBase62Codec: PositiveNumbersBase62Codec

    private val logger = LoggerFactory.getLogger(ReferenceManagerService::class.java)

    fun getReferenceByBase62Id(base62: String): Reference {
        val base10 = try {
            positiveNumbersBase62Codec.decode(base62)
        } catch (e: IllegalStateException) {
            throw WrongUserInputException("Provided link has wrong format")
        }
        val reference = dbService.getByBase10Id(base10)
        logger.info("Trying to retrieve record by id: {} / {}. Success: {}", base10, base62, !isNull(reference))
        return reference ?:
            throw ReferenceNotFoundException("No reference found by provided link")
    }

    fun createNewReference(source: String): Reference {
        val base10 = dbService.getNextSequenceValue()
        val base62 = positiveNumbersBase62Codec.encode(base10)
        logger.info("Generating new reference: {} / {}", base10, base62)
        return dbService.save(Reference(base10, base62, source))
    }
}