package com.banking.bankingservice.dto

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


data class TransactionDTO(
    val id: Long,
    val amount: Double,
    val transactionType: String,
    val description: String?,
    val sourceId: Long?,
    val destinationId: Long,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val createdAt: LocalDateTime
)