package banking.controller

import banking.security.UserPrincipal
import banking.service.TransactionService
import com.banking.bankingservice.dto.DepositSalaryRequest
import com.banking.bankingservice.dto.DepositSalaryResponse
import com.banking.bankingservice.dto.PotDepositRequest
import com.banking.bankingservice.dto.PotDepositResponse
import com.banking.bankingservice.dto.PotWithdrawalRequest
import com.banking.bankingservice.dto.PotWithdrawalResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/v1/salary")
    fun depositSalaryToAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: DepositSalaryRequest
    ): ResponseEntity<DepositSalaryResponse> {
        val transaction =
            transactionService.depositSalaryToAccount(destinationId = request.destinationId, amount = request.amount)

        return ResponseEntity.ok(transaction)
    }

    @PostMapping("/v1/pot/withdrawal")
    fun withdrawalToAccount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PotWithdrawalRequest
    ): ResponseEntity<PotWithdrawalResponse> {
        return ResponseEntity.ok().body(
            transactionService.withdrawFromPotToMainOrSavings(
                request.sourcePotId,
                request.amount,
                principal
            )
        )
    }

    @PostMapping("v1/pot/deposit")
    fun depositToPot(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PotDepositRequest
    ) : ResponseEntity<PotDepositResponse>{

        return ResponseEntity.ok().body(transactionService.manualDepositFromMainOrSavingsToPot(
            sourceAccountId = request.sourceAccountId,
            destinationPotId = request.destinationPotId,
            amount = request.amount,
            principal = principal
        ))

    }


//    @PostMapping("/deposit")
//    fun deposit(
//        @RequestBody request: Map<String, Any>
//    ): ResponseEntity<TransactionEntity> {
//        val accountId = (request["accountId"] as Number).toLong()
//        val amount = (request["amount"] as Number).toDouble()
//        val isSalary = request["isSalary"] as? Boolean ?: false
//        val description = request["description"] as? String
//        val transaction = transactionService.deposit(accountId, amount, isSalary, description)
//        return ResponseEntity.ok(transaction)
//    }
//
//    @PostMapping("/withdraw")
//    fun withdraw(
//        @RequestBody request: Map<String, Any>
//    ): ResponseEntity<TransactionEntity> {
//        val cardId = (request["cardId"] as Number).toLong()
//        val amount = (request["amount"] as Number).toDouble()
//        val description = request["description"] as? String
//        val dateString = request["date"] as? String
//        val parsedDate = dateString?.let { LocalDateTime.parse(it) }
//
//        val transaction = transactionService.withdraw(cardId, amount, description, parsedDate)
//        return ResponseEntity.ok(transaction)
//    }
//
//    @PostMapping("/transfer")
//    fun transfer(
//        @RequestBody request: Map<String, Any>
//    ): ResponseEntity<TransactionEntity> {
//        val sourceId = (request["sourceId"] as Number).toLong() // can be cardnumber or token
//        val amount = (request["amount"] as Number).toDouble()
//        val destinationId = (request["destinationId"] as Number).toLong()
//        val description = request["description"] as? String
//        val dateString = request["date"] as? String
//        val parsedDate = dateString?.let { LocalDateTime.parse(it) }
//        val transaction = transactionService.transfer(sourceId, amount, destinationId, description, parsedDate)
//        return ResponseEntity.ok(transaction)
//    }
//
//    @GetMapping("/{accountId}/history")
//    fun getTransactions(
//        @PathVariable accountId: Long,
//        @RequestParam(required = false, defaultValue = "10") limit: Int
//    ): ResponseEntity<List<TransactionEntity>> {
//        val transactions = transactionService.getTransactions(accountId, limit)
//        return ResponseEntity.ok(transactions)
//    }
}