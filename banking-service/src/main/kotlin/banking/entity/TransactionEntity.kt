package com.banking.bankingservice.entity

import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "transactions")
data class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val cardId: Long? = null,
    val token: String? = null,
    val sourceId: Long? = null, // optional, null for deposits
    val destinationId: Long? = null,
    val amount: Double,
    val transactionType: String,
    val description: String,
    val balance: Double,
    val createdAt: LocalDateTime = LocalDateTime.now()
)