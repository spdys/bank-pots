package banking.dto

import banking.entity.AccountEntity
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateAccountRequest(
    // val userId: Long,
    val accountType: AccountEntity.AccountType
)

data class AccountResponse(
    val id: Long,
    val accountNumber: String,
    val accountType: AccountEntity.AccountType,
    val balance: BigDecimal,
    val createdAt: LocalDateTime
)

data class CloseAccountResponse(
    val accountId: Long,
    val accountNumber: String,
    val accountType: AccountEntity.AccountType,
    val isActive: Boolean
)

data class AccountSummaryDto(
    val accountId: Long,
    val accountNumber: String,
    val accountType: AccountEntity.AccountType,
    val balance: BigDecimal,
    val cardNumber: String?,
    val currency: String,
    val isActive: Boolean,
    val pots: List<PotSummaryDto>? = null
)