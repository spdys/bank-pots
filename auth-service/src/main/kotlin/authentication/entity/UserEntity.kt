package authentication.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var username: String,
    var password: String,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.ROLE_USER,

    val createdAt: LocalDateTime = LocalDateTime.now()



)

enum class Role {
    ROLE_USER,
    ROLE_ADMIN
}