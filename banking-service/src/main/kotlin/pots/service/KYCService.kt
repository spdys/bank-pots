package pots.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import pots.dto.KYCRequest
import pots.entity.KYCEntity
import pots.repository.KYCRepository
import pots.service.validation.KWCivilIDValidator

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

        validateFullName(kycRequest.fullName) // throws IllegalArgumentException | runtime
        KWCivilIDValidator.validate(kycRequest.civilId) // throws PotsException | runtime
        val kyc =
            if (kYCRepository.existsByUserId(kycRequest.userId)) {
                kYCRepository.findByUserId(kycRequest.userId)?.copy()
                    ?: throw EntityNotFoundException("Kyc entity not found")
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
        return ResponseEntity.ok().body("KYC information successfully created/updated")

    }
}