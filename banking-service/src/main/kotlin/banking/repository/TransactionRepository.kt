package banking.repository

import banking.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<TransactionEntity, Long> {
    fun findTop10ByDestinationIdOrderByCreatedAtDesc(destinationId: Long): List<TransactionEntity>
}