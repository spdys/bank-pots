package banking.controller

import banking.entity.TransactionEntity
import banking.service.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/deposit")
    fun deposit(
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<TransactionEntity> {
        val accountId = (request["accountId"] as Number).toLong()
        val amount = (request["amount"] as Number).toDouble()
        val isSalary = request["isSalary"] as? Boolean ?: false
        val description = request["description"] as? String
        val transaction = transactionService.deposit(accountId, amount, isSalary, description)
        return ResponseEntity.ok(transaction)
    }

    @PostMapping("/withdraw")
    fun withdraw(
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<TransactionEntity> {
        val cardId = (request["cardId"] as Number).toLong()
        val amount = (request["amount"] as Number).toDouble()
        val description = request["description"] as? String
        val dateString = request["date"] as? String
        val parsedDate = dateString?.let { LocalDateTime.parse(it) }

        val transaction = transactionService.withdraw(cardId, amount, description, parsedDate)
        return ResponseEntity.ok(transaction)
    }

    @PostMapping("/transfer")
    fun transfer(
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<TransactionEntity> {
        val sourceId = (request["sourceId"] as Number).toLong() // can be cardnumber or token
        val amount = (request["amount"] as Number).toDouble()
        val destinationId = (request["destinationId"] as Number).toLong()
        val description = request["description"] as? String
        val dateString = request["date"] as? String
        val parsedDate = dateString?.let { LocalDateTime.parse(it) }
        val transaction = transactionService.transfer(sourceId, amount, destinationId, description, parsedDate)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping("/{accountId}/history")
    fun getTransactions(
        @PathVariable accountId: Long,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): ResponseEntity<List<TransactionEntity>> {
        val transactions = transactionService.getTransactions(accountId, limit)
        return ResponseEntity.ok(transactions)
    }
}