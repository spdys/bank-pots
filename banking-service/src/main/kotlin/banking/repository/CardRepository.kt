package com.banking.bankingservice.repository

import com.banking.bankingservice.entity.CardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<CardEntity, Long>
