package banking.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import banking.service.validation.DateTimeValid

@Entity
@Table(name = "kyc")
data class KYCEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var userId: Long,

    // Validated in service
    var fullName: String,

    @field:Pattern(
        regexp = "^[9654]\\d{7}$",
        message = "Enter a valid Kuwaiti phone number"
    )
    var phone: String,

    @field:Email(message = "Invalid email format")
    var email: String,

    // Validated in service
    var civilId: String,

    var address: String,

    @field:DateTimeValid(format = "dd-MM-yyyy", message = "dd-MM-yyyy")
    var dateOfBirth: String,

    var verified: Boolean = true
)