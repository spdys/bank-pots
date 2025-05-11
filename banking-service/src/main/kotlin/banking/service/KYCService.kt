package banking.service

import banking.BankingNotFoundException
import banking.dto.KYCFlagResponse
import banking.dto.KYCRequest
import banking.dto.KYCResponse
import banking.entity.KYCEntity
import banking.repository.KYCRepository
import banking.security.UserPrincipal
import banking.service.validation.KWCivilIDValidator
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class KYCService(private val kYCRepository: KYCRepository) {

    fun validateFullName(fullName: String) {
        val trimmed = fullName.trim()
        require(trimmed.isNotBlank()) { "Name cannot be blank" }
        require(trimmed.length >= 3) { "Name should be more than 2 characters" }
        require(!trimmed.any { it.isDigit() }) { "Name should not contain any digits" }
        require(trimmed.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
            "Name can only contain letters, spaces, hyphens, and apostrophes"
        }
    }

    fun createOrUpdateKYC(kycRequest: KYCRequest, principal: UserPrincipal): ResponseEntity<Any> {

        validateFullName(kycRequest.fullName) // throws PotsBadRequestException | runtime
        KWCivilIDValidator.validate(kycRequest.civilId) // throws PotsNotFoundException | runtime

        val kyc =
            if (kYCRepository.existsByUserId(principal.getUserId() ?: -1L)) {
                kYCRepository.findByUserId(principal.getUserId() ?: -1L)?.copy(
                    fullName = kycRequest.fullName,
                    phone = kycRequest.phone,
                    email = kycRequest.email,
                    address = kycRequest.address
                )
                    ?: throw BankingNotFoundException("Kyc entity not found.")
            } else
                KYCEntity(
                    userId = principal.getUserId() ?: -1L,
                    fullName = kycRequest.fullName,
                    phone = kycRequest.phone,
                    email = kycRequest.email,
                    civilId = kycRequest.civilId,
                    address = kycRequest.address,
                    dateOfBirth = kycRequest.dateOfBirth,
                )
        kYCRepository.save(kyc)
        return ResponseEntity.ok(
            KYCResponse(
                kyc.userId,
                kyc.fullName,
                kyc.phone,
                kyc.email,
                kyc.civilId,
                kyc.dateOfBirth,
                kyc.dateOfBirth,
                verified = kyc.verified
            )
        )
    }

    fun flagOrUnflagKYC(userId: Long): ResponseEntity<Any> {

        if (!kYCRepository.existsByUserId(userId))
            throw BankingNotFoundException("KYC entity not found.")
        val kyc = kYCRepository.findByUserId(userId)
        kyc!!.verified = !kyc.verified
        kYCRepository.save(kyc)
        return ResponseEntity.ok()
            .body(
                KYCFlagResponse("User ${kyc.fullName} has been successfully flagged and verified status is now ${kyc.verified}."))
    }

    fun getKYC(userId: Long): ResponseEntity<Any> {
        if (!kYCRepository.existsByUserId(userId))
            throw BankingNotFoundException("KYC entity not found.")
        val kyc = kYCRepository.findByUserId(userId)!!
        return ResponseEntity.ok(
            KYCResponse(
                kyc.userId,
                kyc.fullName,
                kyc.phone,
                kyc.email,
                kyc.civilId,
                kyc.dateOfBirth,
                kyc.dateOfBirth,
                verified = kyc.verified
            )
        )
    }


}