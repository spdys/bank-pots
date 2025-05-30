package banking.service

import banking.BankingBadRequestException
import banking.BankingForbiddenException
import banking.BankingNotFoundException
import banking.dto.PotRequest
import banking.dto.PotResponse
import banking.entity.AccountEntity
import banking.entity.PotEntity
import banking.repository.AccountRepository
import banking.repository.PotRepository
import banking.security.UserPrincipal
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PotService(
    private val accountRepository: AccountRepository,
    private val potRepository: PotRepository,
    private val cardService: CardService
) {
    fun createPot(accountId: Long, request: PotRequest, principal: UserPrincipal): PotResponse {
        // checking if account exists
        val account = accountRepository.findById(accountId)
            .orElseThrow { BankingNotFoundException("Account not found with id $accountId") }

        // check if accountId is associated with principal's ID
        if(account.userId != principal.getId()) {
            throw BankingForbiddenException("User ID mismatch.")
        }

        // checking account type
        if (account.accountType != AccountEntity.AccountType.MAIN) {
            throw BankingBadRequestException("Pots can only be created for MAIN accounts.") as Throwable
        }

        // checking if user already has maximum pots (6)
        val potCount = potRepository.countByAccountId(account.id)

        if (potCount >= 6) {
            throw BankingBadRequestException("This account already has the maximum number of pots (6).")
        }

        // checking if pot name already exists
        val existingPotName = potRepository.findByAccountId(account.id)
            .any { it.name.equals(request.name, ignoreCase = true) }

        if (existingPotName) {
            throw BankingBadRequestException("A pot with name '${request.name}' already exists in this account.")
        }

        // checking if allocation value is positive number
        if (request.allocationValue <= BigDecimal.ZERO) {
            throw BankingBadRequestException("Allocation value cannot be zero or negative.")
        }

        val pot = PotEntity(
            accountId = account.id,
            name = request.name,
            allocationType = request.allocationType,
            allocationValue = request.allocationValue
        )

        val savedPot = potRepository.save(pot)

        // auto generates token card which has pots id embedded in it
        cardService.autoGenerateTokenizedCard(savedPot.id)

        return PotResponse(
            savedPot.id,
            savedPot.name,
            savedPot.balance,
            savedPot.allocationType,
            savedPot.allocationValue
        )
    }

    fun editPot(accountId: Long, potId: Long, request: PotRequest, principal: UserPrincipal): PotResponse{
        // checking if account exists
        val account = accountRepository.findById(accountId)
            .orElseThrow { BankingNotFoundException("Account not found with id $accountId.") }

        // check if accountId is associated with principal's ID
        if(account.userId != principal.getId()) {
            throw BankingForbiddenException("User ID mismatch.")
        }

        // checking account type
        if (account.accountType != AccountEntity.AccountType.MAIN) {
            throw BankingBadRequestException("Pots can only be edited for MAIN accounts.")
        }

        // checking if pot exists
        val pot = potRepository.findById(potId)
            .orElseThrow { BankingNotFoundException("Pot not found with id $potId.") }

        // checking if pot id belongs to account
        if (pot.accountId != account.id) {
            throw BankingBadRequestException("Pot does not belong to account $accountId.")
        }

        // checking if pot name already used
        val conflictingName = potRepository.findByAccountId(account.id)
            .any { it.name.equals(request.name, ignoreCase = true) && it.id != potId }

        if (conflictingName) {
            throw BankingBadRequestException("Another pot with name '${request.name}' already exists in this account.")
        }

        if (request.allocationValue <= BigDecimal.ZERO) throw BankingBadRequestException("Allocation value cannot be zero or negative.")

        val updatedPot = pot.copy(
            name = request.name,
            allocationType = request.allocationType,
            allocationValue = request.allocationValue
        )

        val savedPot = potRepository.save(updatedPot)

        return PotResponse(
            savedPot.id,
            savedPot.name,
            savedPot.balance,
            savedPot.allocationType,
            savedPot.allocationValue
        )

    }
}