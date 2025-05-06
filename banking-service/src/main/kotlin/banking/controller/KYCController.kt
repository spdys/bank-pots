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

@RestController
@RequestMapping("/api/v1/kyc")
class KYCController(kycService: KYCService, private val kYCService: KYCService) {


    @PostMapping
    fun submitKYC(@RequestBody kycRequest: KYCRequest): ResponseEntity<Any> {
        return kYCService.createOrUpdateKYC(kycRequest)
    }

    @GetMapping("/{userId}")
    fun getKYC(@PathVariable userId: Long): ResponseEntity<Any> {
        return kYCService.getKYC(userId)
    }

    // Re-send request to unflag
    // TODO(Make it for admins only!)
    @PostMapping("/flag")
    fun flagKYC(@RequestBody userIdRequest: KYCFlagRequest): ResponseEntity<Any> {
        return kYCService.flagOrUnflagKYC(userIdRequest.userId)
    }



}