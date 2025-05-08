package banking.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cards")
data class CardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var cardNumber: String,
    var token: String? = null,
    val cardType: String,
    var isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "account_id")
    val account: AccountEntity? = null,

    @ManyToOne
    @JoinColumn(name = "pot_id")
    val pot: PotEntity? = null
)
