package banking.controller

import banking.dto.CardPaymentRequest
import banking.dto.CardPaymentResponse
import banking.dto.DepositSalaryRequest
import banking.dto.DepositSalaryResponse
import banking.dto.PotDepositRequest
import banking.dto.PotDepositResponse
import banking.dto.PotWithdrawalRequest
import banking.dto.PotWithdrawalResponse
import banking.dto.TransactionHistoryRequest
import banking.dto.TransactionHistoryResponse
import banking.security.UserPrincipal
import banking.service.TransactionService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import banking.dto.FailureResponse
import io.swagger.v3.oas.annotations.media.Schema

@RestController
@Tag(name = "Transactions API", description = "Salary deposit, purchases, and pot transactions")
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @Operation(summary = "Deposit salary to account (Admin only)")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = DepositSalaryResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
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

    @Operation(summary = "Withdraw from pot to main account")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = PotWithdrawalResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("/v1/pot/withdrawal")
    fun withdrawalToAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PotWithdrawalRequest
    ): ResponseEntity<PotWithdrawalResponse> {
        return ResponseEntity.ok().body(
            transactionService.withdrawFromPotToMain(
                request.sourcePotId,
                request.amount,
                principal
            )
        )
    }


    @Operation(summary = "Deposit to pot" , description = "Adds funds to a pot from main/savings account")
    @ApiResponses(
        ApiResponse(responseCode = "200",content = [Content(schema = Schema(implementation = PotDepositResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
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

    @Operation(summary = "Purchase from card or tokenized card", description = "Make POS purchases")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = CardPaymentResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("v1/purchase")
    fun purchaseFromCard(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CardPaymentRequest
    ): ResponseEntity<CardPaymentResponse> {
        return ResponseEntity.ok()
            .body(
                transactionService
                    .cardPurchase(
                        request.cardNumberOrToken,
                        request.amount,
                        request.destinationId,
                        principal)
            )
    }

    @Operation(summary = "Retrieve transaction history", description = "Retrieve history per pot, per card, or per account")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TransactionHistoryResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("v1/history")
    fun retrieveTransactionHistory(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: TransactionHistoryRequest
    ) : ResponseEntity<List<TransactionHistoryResponse>?> {

        return ResponseEntity.ok().body(transactionService.transactionHistory(
            accountId = request.accountId,
            potId = request.potId,
            cardId = request.cardId,
            principal = principal
        ))
    }

}