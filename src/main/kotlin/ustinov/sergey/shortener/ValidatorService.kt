package ustinov.sergey.shortener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ValidatorService {

    @Autowired
    private lateinit var cfg: ApplicationConfigurer

    fun validateHttpReference(input: String): Boolean {
        return true
    }

    fun isDomainAllowed(input: String): Boolean {
        for (url in cfg.getServerURLs()) {
            if (input.startsWith(url)) {
                return false
            }
        }
        return true
    }

    fun hasWhitespace(input: String): Boolean {
        for (c in input) {
            if (c.isWhitespace()) {
                return true
            }
        }
        return false
    }
}