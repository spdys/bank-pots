package banking.dto

data class CardDTO(
    val accountId: Long?,   // to be replaced
    val potId: Long?,       // only for virtual cards
    val cardType: String    // phycisal or virtual
)
