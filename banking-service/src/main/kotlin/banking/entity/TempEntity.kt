package com.banking.bankingservice.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Account(
    @Id val tempId: Long = 0
)

@Entity
data class Pot(
    @Id val tempId: Long = 0
)
