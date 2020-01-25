package ustinov.sergey.shortener

import org.springframework.stereotype.Component

@Component
class ValidatorService {

    fun isReferenceAlreadyShorten(input: String): Boolean {
        return false
    }

    fun validateHttpReference(input: String): Boolean {
        return true
    }
}