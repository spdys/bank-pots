package banking.controller

import banking.dto.*
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
        ApiResponse(
            responseCode = "200",
            description = "Salary successfully deposited.",
            content = [Content(schema = Schema(implementation = DepositSalaryResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid input or business rule violation (e.g., account inactive, not main account).",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Account not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: admin access required.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
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
        ApiResponse(
            responseCode = "200",
            description = "Withdrawal from pot to main account successful.",
            content = [Content(schema = Schema(implementation = PotTransferResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid withdrawal request. This may occur if the pot/account is inactive, the amount is negative or exceeds the balance, or if the account is misconfigured.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: the authenticated user's ID does not match the pot/account owner.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "The specified pot or account was not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("/v1/pot/withdrawal")
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


    @Operation(summary = "Deposit to pot" , description = "Adds funds to a pot from main/savings account")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Funds successfully deposited into the pot.",
            content = [Content(schema = Schema(implementation = PotDepositResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid deposit request. This may occur if the pot/account is mismatched, inactive, or if the amount is invalid.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: the authenticated user's ID does not match the source account owner.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "The specified account or pot was not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
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
        ApiResponse(
            responseCode = "200",
            description = "Purchase completed successfully.",
            content = [Content(schema = Schema(implementation = CardPaymentResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Bad request. This may include invalid card, insufficient balance, or expired card.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: user ID in the token does not match the card owner, or the account is inactive.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Card, pot, or account not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
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
                        principal
                    )
            )
    }

    @Operation(summary = "Retrieve transaction history", description = "Retrieve history per pot, per card, or per account")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Transaction history retrieved successfully.",
            content = [Content(schema = Schema(implementation = TransactionHistoryResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Bad request. You must provide exactly one of: cardId, potId, or accountId.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: user ID in token does not match the requested resource.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Specified card, pot, or account not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
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