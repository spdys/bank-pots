package banking.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cards")
data class CardEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // Fks
    var accountId: Long? = null,
    var potId: Long? = null,
    var cardNumber: String? = null,
    var token: String? = null,

    @Enumerated(EnumType.STRING)
    var cardType: CardType,

    var isActive: Boolean = true,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var expiresAt: LocalDateTime = LocalDateTime.now().plusYears(3)
)

enum class CardType {
    PHYSICAL,
    TOKENIZED
}