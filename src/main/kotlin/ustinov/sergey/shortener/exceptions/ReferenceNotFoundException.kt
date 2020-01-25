package ustinov.sergey.shortener.exceptions

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.Exception

@ResponseStatus(NOT_FOUND)
class ReferenceNotFoundException(message: String) : Exception(message)