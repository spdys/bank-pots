package banking.entity

import banking.BankingBadRequestException
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "accounts")
data class AccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    val accountNumber: String,

    @Enumerated(EnumType.STRING)
    val accountType: AccountType,

    var balance: BigDecimal = BigDecimal.ZERO,
    val currency: String = "KWD",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
) {
    enum class AccountType {
        MAIN,
        SAVINGS
    }
    @PrePersist
    @PreUpdate
    fun validateBalance() {
        if (balance < BigDecimal.ZERO) {
            throw BankingBadRequestException("Account balance cannot be negative.")
        }
    }
}
