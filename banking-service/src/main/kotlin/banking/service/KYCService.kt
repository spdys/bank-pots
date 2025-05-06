package banking.service

import banking.BankingNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import banking.dto.KYCRequest
import banking.dto.KYCResponse
import banking.entity.KYCEntity
import banking.repository.KYCRepository
import banking.service.validation.KWCivilIDValidator

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

    fun createOrUpdateKYC(kycRequest: KYCRequest): ResponseEntity<Any> {

        validateFullName(kycRequest.fullName) // throws PotsBadRequestException | runtime
        KWCivilIDValidator.validate(kycRequest.civilId) // throws PotsNotFoundException | runtime
        val kyc =
            if (kYCRepository.existsByUserId(kycRequest.userId)) {
                kYCRepository.findByUserId(kycRequest.userId)?.copy(
                    fullName = kycRequest.fullName,
                    phone = kycRequest.phone,
                    email = kycRequest.email,
                    address = kycRequest.address
                )
                    ?: throw BankingNotFoundException("Kyc entity not found.")
            } else
                KYCEntity(
                    userId = kycRequest.userId,
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
            .body("User ${kyc.fullName} has been successfully flagged and verified status is now ${kyc.verified}.")
    }

    fun getKYC(userId: Long): ResponseEntity<Any> {
        if (!kYCRepository.existsByUserId(userId))
            throw BankingNotFoundException("KYC entity not found,.")
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
            ))
    }


}