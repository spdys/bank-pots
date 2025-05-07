package com.banking.bankingservice.service

import banking.BankingBadRequestException
import com.banking.bankingservice.entity.TransactionEntity
import com.banking.bankingservice.repository.TransactionRepository
import banking.BankingNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {

    fun deposit(accountId: Long, amount: Double, isSalary: Boolean = false): TransactionEntity {
        if (amount <= 0) throw BankingBadRequestException("Deposit amount must be positive")

        // temp balance simulation (to be replaced with  account service)
        val tempBalanceBefore = 1000.0 // to be replaced
        val tempBalanceAfter = tempBalanceBefore + amount

        val transactionType = if (isSalary) "SALARY" else "DEPOSIT"

        val transaction = TransactionEntity(
            amount = amount,
            transactionType = transactionType,
            description = if (isSalary) "Salary deposit" else "General deposit",
            destinationId = accountId,
            balanceBefore = tempBalanceBefore,
            balanceAfter = tempBalanceAfter,
            createdAt = LocalDateTime.now()
        )
        return transactionRepository.save(transaction)
    }
}