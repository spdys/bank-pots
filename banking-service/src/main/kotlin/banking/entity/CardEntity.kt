package com.banking.bankingservice.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cards")
data class CardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val cardNumber: String,
    val token: String? = null,
    val cardType: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "account_id")
    val account: Account? = null,

    @ManyToOne
    @JoinColumn(name = "pot_id")
    val pot: Pot? = null
)
