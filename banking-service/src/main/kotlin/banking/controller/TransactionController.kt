package com.banking.bankingservice.controller

import com.banking.bankingservice.entity.TransactionEntity
import com.banking.bankingservice.service.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService)
{
    @PostMapping("/deposit/{accountId}")
    fun deposit(
        @PathVariable accountId: Long,
        @RequestParam amount: Double,
        @RequestParam(required = false) isSalary: Boolean = false
    ): ResponseEntity<TransactionEntity> {
        val transaction = transactionService.deposit(accountId, amount, isSalary)
        return ResponseEntity.ok(transaction)
    }
}