package pots.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import pots.dto.KYCInfo
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

    fun createOrUpdateKYC(kycInfo: KYCInfo): ResponseEntity<Any> {

        validateFullName(kycInfo.fullName) // throws IllegalArgumentException | runtime
        KWCivilIDValidator.validate(kycInfo.civilId) // throws PotsException | runtime
        val kyc =
            if (kYCRepository.existsByUserId(kycInfo.userId)) {
                kYCRepository.findByUserId(kycInfo.userId)?.copy(
                    fullName = kycInfo.fullName,
                    phone = kycInfo.phone,
                    email = kycInfo.email,
                    address = kycInfo.address
                )
                    ?: throw EntityNotFoundException("Kyc entity not found")
            } else
                KYCEntity(
                    userId = kycInfo.userId,
                    fullName = kycInfo.fullName,
                    phone = kycInfo.phone,
                    email = kycInfo.email,
                    civilId = kycInfo.civilId,
                    address = kycInfo.address,
                    dateOfBirth = kycInfo.dateOfBirth,
                )
        kYCRepository.save(kyc)
        return ResponseEntity.ok(
            kyc)
    }
}