package banking.dto

import java.math.BigDecimal


data class CardPaymentRequest(
    val cardNumberOrToken: String,
    val amount: BigDecimal,
)
data class CardPaymentResponse(
    val newBalance: BigDecimal,
)
data class CardDTO(
    val accountId: Long?,   // to be replaced
    val potId: Long?,       // only for virtual cards
    val cardType: String    // phycisal or virtual
)
