package banking.repository

import banking.entity.PotEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PotRepository : JpaRepository<PotEntity, Long> {
    fun findByAccountId(accountId: Long): List<PotEntity>
    fun countByAccountId(accountId: Long): Int
}
