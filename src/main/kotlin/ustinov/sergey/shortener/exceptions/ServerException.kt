package ustinov.sergey.shortener.exceptions

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(INTERNAL_SERVER_ERROR)
class ServerException(message: String) : AbstractSystemException(message)