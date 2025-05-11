package banking.dto

import banking.entity.PotEntity
import java.math.BigDecimal

data class PotSummaryDto(
    val potId: Long,
    val name: String,
    val balance: BigDecimal,
    val cardToken: String?,
    val allocationType: PotEntity.AllocationType,
    val allocationValue: BigDecimal
)

data class PotRequest(
    val name: String,
    val allocationType: PotEntity.AllocationType,
    val allocationValue: BigDecimal
)

data class PotResponse(
    val potId: Long,
    val name: String,
    val balance: BigDecimal,
    val allocationType: PotEntity.AllocationType,
    val allocationValue: BigDecimal
)
