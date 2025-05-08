package com.banking.bankingservice.repository

import com.banking.bankingservice.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<TransactionEntity, Long> {
    fun findTop10ByDestinationIdOrderByCreatedAtDesc(destinationId: Long): List<TransactionEntity>
}