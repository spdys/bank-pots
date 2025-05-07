package banking.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "pots")
data class PotEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val accountId: Long? = null,
    val name: String,
    val balance: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    val allocationType: AllocationType,

    val allocationValue: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    enum class AllocationType {
        FIXED,
        PERCENTAGE
    }
}
