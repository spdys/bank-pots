package banking.repository

import banking.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, Long> {
    fun findByUserId(userId: Long): List<AccountEntity>
}
