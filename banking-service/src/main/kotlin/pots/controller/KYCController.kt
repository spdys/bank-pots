package pots.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pots.dto.KYCFlagRequest
import pots.dto.KYCInfo
import pots.service.KYCService

@RestController
@RequestMapping("/api/v1/kyc")
class KYCController(kycService: KYCService, private val kYCService: KYCService) {


    @PostMapping
    fun submitKYC(@RequestBody kycInfo: KYCInfo): ResponseEntity<Any> {
        return kYCService.createOrUpdateKYC(kycInfo)
    }

    @PostMapping("/flag")
    fun flagKYC(@RequestBody userIdRequest: KYCFlagRequest): ResponseEntity<Any> {
        return kYCService.flagKYC(userIdRequest.userId)
    }

}