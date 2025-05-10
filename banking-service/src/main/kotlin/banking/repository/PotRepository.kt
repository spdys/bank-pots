package banking.repository

import banking.entity.PotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PotRepository : JpaRepository<PotEntity, Long> {
    fun findByAccountId(accountId: Long): List<PotEntity>
    fun countByAccountId(accountId: Long): Int
    fun findAllByAccountId(accountId: Long): List<PotEntity>
}
