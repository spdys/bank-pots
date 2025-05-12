package banking.controller

import banking.dto.FailureResponse
import banking.dto.KYCRequest
import banking.dto.KYCResponse
import banking.security.UserPrincipal
import banking.service.KYCService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@RestController
@Tag(name = "KYC API")
@RequestMapping("/api/v1/kyc")
class KYCController(kycService: KYCService, private val kYCService: KYCService) {


    @Operation(summary = "Submit KYC")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "KYC successfully submitted or updated.",
            content = [Content(schema = Schema(implementation = KYCResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid input data (e.g. invalid name or civil ID).",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Associated user or data not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PostMapping
    fun submitKYC(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody kycRequest: KYCRequest
    ): ResponseEntity<Any> {
        return kYCService.createOrUpdateKYC(kycRequest, principal)
    }


    @Operation(summary = "Get KYC")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "KYC record successfully retrieved.",
            content = [Content(schema = Schema(implementation = KYCResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "KYC record not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @GetMapping
    fun getKYC(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<Any> {
        return kYCService.getKYC(principal.getId() ?: 0) // no id == 0
    }


    @Operation(summary = "Flag or unflag KYC (Admin Only)")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Returns a confirmation message with the user's full name and new verification status.",
            content = [Content(schema = Schema(type = "string"))]),
        ApiResponse(
            responseCode = "403",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/flag/{targetUserId}")
    fun flagKYC(@PathVariable targetUserId: Long): ResponseEntity<Any> {
        return kYCService.flagOrUnflagKYC(targetUserId)
    }
}