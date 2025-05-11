package banking

open class BankingBadRequestException(message: String): RuntimeException(message)
open class BankingNotFoundException(message: String): RuntimeException(message)
open class BankingForbiddenException(message: String): RuntimeException(message)