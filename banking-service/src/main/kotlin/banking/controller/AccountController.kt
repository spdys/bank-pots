package banking.controller

import banking.dto.AccountResponse
import banking.dto.AccountSummaryDto
import banking.dto.CloseAccountResponse
import banking.dto.CreateAccountRequest
import banking.dto.PotRequest
import banking.dto.PotResponse
import banking.service.AccountService
import banking.service.PotService
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal

@RestController
class AccountController(
    private val accountService: AccountService,
    private val potService: PotService
) {
    @PostMapping("/accounts/v1/create")
    fun createAccount(
        @AuthenticationPrincipal principal: banking.security.UserPrincipal,
        @RequestBody request: CreateAccountRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.createAccount(request, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/accounts/v1/{accountId}/pots")
    fun createPot(
        @AuthenticationPrincipal principal: banking.security.UserPrincipal,
        @PathVariable accountId: Long,
        @RequestBody request: PotRequest
    ): ResponseEntity<PotResponse> {
        val response = potService.createPot(accountId, request, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/accounts/v1/{accountId}/pots/{potId}")
    fun editPot(
        @AuthenticationPrincipal principal: banking.security.UserPrincipal,
        @PathVariable accountId: Long,
        @PathVariable potId: Long,
        @RequestBody request: PotRequest
    ): ResponseEntity<PotResponse> {
        val response = potService.editPot(accountId, potId, request, principal)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/accounts/v1/{accountId}/summary")
    fun getAccountSummary(
        @AuthenticationPrincipal principal: banking.security.UserPrincipal,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountSummaryDto> {
        val response = accountService.getAccountSummary(accountId, principal)
        return ResponseEntity.ok(response)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/v1/accounts/{accountId}/close")
    fun closeAccount(
        @AuthenticationPrincipal principal: banking.security.UserPrincipal,
        @PathVariable accountId: Long
    ): ResponseEntity<CloseAccountResponse> {
        val response = accountService.closeAccount(accountId)
        return ResponseEntity.ok(response)
    }
}
