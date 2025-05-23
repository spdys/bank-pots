package banking.repository

import banking.entity.CardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<CardEntity, Long>{

    fun findByCardNumber(cardNumberOrToken: String): CardEntity?
    fun findByToken(token: String): CardEntity?
    fun findByAccountId(accountId: Long): CardEntity?
    fun findByPotId(potId: Long): CardEntity?

}
