package banking.controller

import banking.dto.AccountResponse
import banking.dto.AccountSummaryDto
import banking.dto.CloseAccountResponse
import banking.dto.CreateAccountRequest
import banking.dto.PotRequest
import banking.dto.PotResponse
import banking.security.UserPrincipal
import banking.service.AccountService
import banking.service.PotService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import banking.dto.FailureResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal


@RestController
@Tag(name = "Account and Pots API")
class AccountController(
    private val accountService: AccountService,
    private val potService: PotService
) {

    @Operation(summary = "Create a new account for the authenticated user")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Account created successfully.",
            content = [Content(schema = Schema(implementation = AccountResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid account creation request.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("/accounts/v1/create")
    fun createAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateAccountRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.createAccount(request, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }


    @Operation(summary = "Create a new pot within the user's account")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Pot created successfully.",
            content = [Content(schema = Schema(implementation = PotResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid pot creation request.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: account does not belong to user.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Account not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("/accounts/v1/{accountId}/pots")
    fun createPot(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable accountId: Long,
        @RequestBody request: PotRequest
    ): ResponseEntity<PotResponse> {
        val response = potService.createPot(accountId, request, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }


    @Operation(summary = "Edit an existing pot within the user's account")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Pot edited successfully.",
            content = [Content(schema = Schema(implementation = PotResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid pot edit request.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: account does not belong to user.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Account or pot not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping("/accounts/v1/{accountId}/pots/{potId}")
    fun editPot(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable accountId: Long,
        @PathVariable potId: Long,
        @RequestBody request: PotRequest
    ): ResponseEntity<PotResponse> {
        val response = potService.editPot(accountId, potId, request, principal)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Get a summary of the user's account including pots and balance")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved account summary.",
            content = [Content(schema = Schema(implementation = AccountSummaryDto::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: account does not belong to user.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Account not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @GetMapping("/accounts/v1/{accountId}/summary")
    fun getAccountSummary(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountSummaryDto> {
        val response = accountService.getAccountSummary(accountId, principal)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Close a user account (Admin only)")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Account successfully closed.",
            content = [Content(schema = Schema(implementation = CloseAccountResponse::class))]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: only admins may perform this action.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Account not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/v1/accounts/{accountId}/close")
    fun closeAccount(@PathVariable accountId: Long): ResponseEntity<CloseAccountResponse> {
        val response = accountService.closeAccount(accountId)
        return ResponseEntity.ok(response)
    }
}
