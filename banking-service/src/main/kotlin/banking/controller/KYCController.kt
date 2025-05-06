package banking.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import banking.dto.KYCFlagRequest
import banking.dto.KYCRequest
import banking.service.KYCService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestAttribute
import java.nio.file.attribute.UserPrincipal
import java.security.Principal

@RestController
@RequestMapping("/api/v1/kyc")
class KYCController(kycService: KYCService, private val kYCService: KYCService) {


    // TODO
    @PostMapping
    fun submitKYC(@RequestBody kycRequest: KYCRequest): ResponseEntity<Any> {
        return kYCService.createOrUpdateKYC(kycRequest)
    }

    // TODO
    @GetMapping("/{userId}")
    fun getKYC(@PathVariable userId: Long): ResponseEntity<Any> {
        return kYCService.getKYC(userId)
    }

    // Re-send request to unflag
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/flag")
    fun flagKYC(@AuthenticationPrincipal  principal: banking.security.UserPrincipal): ResponseEntity<Any> {
        println(principal.getId().toString())
        return kYCService.flagOrUnflagKYC(principal.getId()!!)
    }



}