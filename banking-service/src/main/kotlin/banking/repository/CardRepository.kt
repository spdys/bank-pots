package banking.repository

import banking.entity.CardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<CardEntity, Long>{
    //fun findCardByUserId(userId: Long): CardEntity?
}
