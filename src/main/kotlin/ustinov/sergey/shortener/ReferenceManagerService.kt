package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ustinov.sergey.shortener.exceptions.ReferenceNotFoundException
import ustinov.sergey.shortener.exceptions.WrongUserInputException
import ustinov.sergey.shortener.model.Reference

@Component
class ReferenceManagerService {
    @Autowired
    private lateinit var dbService: DBService
    @Autowired
    private lateinit var positiveNumbersBase62Codec: PositiveNumbersBase62Codec

    fun getReferenceByBase62Id(base62: String): Reference {
        val base10 = try {
            positiveNumbersBase62Codec.decode(base62)
        } catch (e: IllegalStateException) {
            throw WrongUserInputException("Provided link has wrong format")
        }
        return dbService.getByBase10Id(base10) ?:
            throw ReferenceNotFoundException("No reference found by provided link")
    }

    fun createNewReference(source: String): Reference {
        val base10 = dbService.getNextSequenceValue()
        val base62 = positiveNumbersBase62Codec.encode(base10)
        return dbService.save(Reference(base10, base62, source))
    }
}