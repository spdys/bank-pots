package com.banking.bankingservice.dto

import java.time.LocalDateTime

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