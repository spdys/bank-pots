package pots.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import pots.service.validation.DateTimeValid

@Entity
@Table(name = "kyc")
data class KYCEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var userId: Long,

    var fullName: String,
    var phone: String,

    @field:Email(message = "Invalid email format")
    var email: String,

    var civilId: String,
    var address: String,

    @field:DateTimeValid(format = "dd-MM-yyyy", message = "dd-MM-yyyy")
    var dateOfBirth: String,

    var verified: Boolean = true
)