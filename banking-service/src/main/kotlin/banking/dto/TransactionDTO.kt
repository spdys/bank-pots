package banking.dto

import banking.entity.TransactionEntity
import java.math.BigDecimal
import java.time.LocalDateTime


data class DepositSalaryRequest(
    val destinationId: Long,
    val amount: BigDecimal,
)

data class DepositSalaryResponse(
    val destinationId: Long,
    val balanceBefore: BigDecimal,
    val balanceAfter: BigDecimal,
)

data class PotWithdrawalRequest(
    val sourcePotId: Long,
    val amount: BigDecimal
)

data class PotWithdrawalResponse(
    val newPotBalance: BigDecimal,
    val newAccountBalance: BigDecimal,
)

data class PotDepositRequest(
    val sourceAccountId: Long,
    val destinationPotId: Long,
    val amount: BigDecimal,
)

data class PotDepositResponse(
    val newPotBalance: BigDecimal,
    val newAccountBalance: BigDecimal
)

data class TransactionHistoryRequest(
    val cardId: Long? = null,
    val accountId: Long? = null,
    val potId: Long? = null,
)
data class TransactionHistoryResponse(
    val id: Long,
    val amount: BigDecimal,
    val transactionType: String,
    val description: String?,
    val createdAt: LocalDateTime
)
fun TransactionEntity.toHistoryResponse() = TransactionHistoryResponse(
    id = this.id!!,
    amount = this.amount,
    transactionType = this.transactionType.name,
    description = this.description,
    createdAt = this.createdAt
)
