package banking.repository

import banking.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, Long> {
    fun findAllByUserId(userId: Long): List<AccountEntity>
    fun findByUserId(userId: Long): AccountEntity?
}
