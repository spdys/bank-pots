package banking.controller

import banking.dto.*
import banking.security.UserPrincipal
import banking.service.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/v1/salary")
    fun depositSalaryToAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: DepositSalaryRequest
    ): ResponseEntity<DepositSalaryResponse> {
        val transaction =
            transactionService.depositSalaryToAccount(destinationId = request.destinationId, amount = request.amount)

        return ResponseEntity.ok(transaction)
    }

    @PostMapping("/v1/pot/withdrawal") //renamed transfer in other functions
    fun withdrawalToAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PotTransferRequest
    ): ResponseEntity<PotTransferResponse> {
        return ResponseEntity.ok().body(
            transactionService.transferFromPotToMain(
                request.sourcePotId,
                request.amount,
                principal
            )
        )
    }

    @PostMapping("v1/pot/deposit")
    fun depositToPot(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PotDepositRequest
    ): ResponseEntity<PotDepositResponse> {

        return ResponseEntity.ok().body(
            transactionService.manualDepositFromMainOrSavingsToPot(
                sourceAccountId = request.sourceAccountId,
                destinationPotId = request.destinationPotId,
                amount = request.amount,
                principal = principal
            )
        )

    }
}
//
//    @PostMapping("v1/purchase") //withdraw from pot or main
//    fun purchaseFromCard(
//        @AuthenticationPrincipal principal: UserPrincipal,
//        @RequestBody request: CardPaymentRequest
//    ): ResponseEntity<CardPaymentResponse> {
//        return ResponseEntity.ok()
//            .body(
//                transactionService
//                    .cardPurchase(
//                        request.cardNumberOrToken,
//                        request.amount,
//                        request.destinationId,
//                        principal
//                    )
//            )
//    }
//}
//
//    @PostMapping("v1/history")
//   fun retrieveTransactionHistory(
//       @AuthenticationPrincipal principal: UserPrincipal,
//       @RequestBody request: TransactionHistoryRequest
//   ) : ResponseEntity<List<TransactionHistoryResponse>?> {
//
//       return ResponseEntity.ok().body(transactionService.transactionHistory(
//            accountId = request.accountId,
//            potId = request.potId,
//            cardId = request.cardId,
//           principal = principal
//        ))
//    }
//
//}