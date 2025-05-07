package banking.controller

import banking.dto.KYCRequest
import banking.security.UserPrincipal
import banking.service.KYCService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/kyc")
class KYCController(kycService: KYCService, private val kYCService: KYCService) {



    @PostMapping
    fun submitKYC(
        @AuthenticationPrincipal principal: banking.security.UserPrincipal,
        @RequestBody kycRequest: KYCRequest
    ): ResponseEntity<Any> {
        return kYCService.createOrUpdateKYC(kycRequest, principal)
    }

    @GetMapping
    fun getKYC(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<Any> {
        return kYCService.getKYC(principal.getUserId() ?: 0) // no id == 0
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/flag")
    fun flagKYC(@AuthenticationPrincipal principal: banking.security.UserPrincipal): ResponseEntity<Any> {
        println(principal.getId().toString())
        return kYCService.flagOrUnflagKYC(principal.getId()!!)
    }


}