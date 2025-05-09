package banking.entity

import banking.BankingBadRequestException
import java.time.LocalDateTime
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "transactions")
data class TransactionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var sourceId: Long? = null,
    var destinationId: Long,

    var amount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    var transactionType: TransactionType,
    var description: String? = null,
    var balanceBefore: BigDecimal,
    var balanceAfter: BigDecimal,
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var cardId: Long? = null, // to reference card

    ) {

enum class TransactionType{
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    PURCHASE,
}
    @PrePersist
    @PreUpdate
    fun validateBalance() {
        if (amount < BigDecimal.ZERO) {
            throw BankingBadRequestException("Amount cannot be negative.")
        }

    }
}