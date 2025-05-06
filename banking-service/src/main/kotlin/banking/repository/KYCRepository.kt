package banking.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import banking.entity.KYCEntity

@Repository
interface KYCRepository : JpaRepository<KYCEntity, Long> {
    fun findByUserId(userId: Long): KYCEntity?
    fun existsByUserId(userId: Long): Boolean
}