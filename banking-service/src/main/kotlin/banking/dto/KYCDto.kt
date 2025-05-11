package banking.dto



data class KYCRequest(
//    val userId: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val civilId: String,
    val address: String,
    val dateOfBirth: String,
)

data class KYCResponse(
    val userId: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val civilId: String,
    val address: String,
    val dateOfBirth: String,
    val verified: Boolean,
)

data class KYCFlagResponse(val response: String)





