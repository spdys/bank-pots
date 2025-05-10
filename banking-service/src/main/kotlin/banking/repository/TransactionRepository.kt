package banking.repository

import banking.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<TransactionEntity, Long> {
    fun findAllByCardId(cardId: Long): List<TransactionEntity>
    fun findAllBySourceId(sourceId: Long): List<TransactionEntity>
    fun findAllByDestinationId(destinationId: Long): List<TransactionEntity>

    fun findTop10ByDestinationIdOrderByCreatedAtDesc(destinationId: Long): List<TransactionEntity>
}