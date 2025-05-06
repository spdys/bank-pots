package pots.dto



data class KYCInfo(
    val userId: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val civilId: String,
    val address: String,
    val dateOfBirth: String,
)

data class KYCFlagRequest(val userId: Long)



