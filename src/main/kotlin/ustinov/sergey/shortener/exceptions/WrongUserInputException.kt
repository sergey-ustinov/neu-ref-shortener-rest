package ustinov.sergey.shortener.exceptions

import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(BAD_REQUEST)
class WrongUserInputException(message: String) : Exception(message)