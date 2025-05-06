package banking

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

open class PotsException(message: String) : RuntimeException(message)

