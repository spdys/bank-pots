package com.banking.bankingservice.entity

import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "transactions")
data class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val amount: Double,
    val transactionType: String,
    val description: String? = null,
    val sourceId: Long? = null,
    val destinationId: Long,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val createdAt: LocalDateTime = LocalDateTime.now()
)