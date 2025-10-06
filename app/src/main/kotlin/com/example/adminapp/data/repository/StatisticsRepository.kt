package com.example.adminapp.data.repository

import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.*
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatisticsRepository {

    suspend fun getStatistics(): StatisticsData {
        return try {
            println("=== FETCHING STATISTICS DATA ===")
            
            // Fetch completed transactions with booking details
            val completedTransactions = getCompletedTransactions()
            println("Found ${completedTransactions.size} completed transactions")
            
            // Group by date for daily revenue
            val dailyRevenues = groupTransactionsByDate(completedTransactions)
            println("Generated ${dailyRevenues.size} daily revenue records")
            
            // Group by date for daily orders count
            val dailyOrders = groupOrdersByDate(completedTransactions)
            println("Generated ${dailyOrders.size} daily order records")
            
            // Create transaction summaries - filter out transactions without booking ID
            val transactionSummaries = completedTransactions
                .filter { it.bookingId != null }
                .map { transaction ->
                    TransactionSummary(
                        bookingId = transaction.bookingId!!,
                        paymentMethod = transaction.paymentMethod,
                        amount = transaction.amount,
                        createdAt = transaction.createdAt,
                        revenue = transaction.amount * 0.2
                    )
                }
            
            val totalRevenue = dailyRevenues.sumOf { it.totalRevenue }
            val totalCompletedOrders = dailyOrders.sumOf { it.completedOrders }
            
            StatisticsData(
                dailyRevenues = dailyRevenues,
                dailyOrders = dailyOrders,
                transactionSummaries = transactionSummaries,
                totalRevenue = totalRevenue,
                totalCompletedOrders = totalCompletedOrders
            )
            
        } catch (e: Exception) {
            println("ERROR fetching statistics: ${e.message}")
            e.printStackTrace()
            StatisticsData(
                dailyRevenues = emptyList(),
                dailyOrders = emptyList(),
                transactionSummaries = emptyList(),
                totalRevenue = 0.0,
                totalCompletedOrders = 0
            )
        }
    }
    
    private suspend fun getCompletedTransactions(): List<Transaction> {
        return try {
            // Query transactions with status = 'completed'
            supabase.from("transactions")
                .select {
                    filter {
                        eq("status", "completed")
                    }
                }
                .decodeList<Transaction>()
        } catch (e: Exception) {
            println("Error fetching completed transactions: ${e.message}")
            
            // Fallback: get all transactions
            try {
                supabase.from("transactions")
                    .select()
                    .decodeList<Transaction>()
            } catch (e2: Exception) {
                println("Fallback query also failed: ${e2.message}")
                emptyList()
            }
        }
    }
    
    private fun groupTransactionsByDate(transactions: List<Transaction>): List<DailyRevenue> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        return transactions
            .groupBy { transaction ->
                // Extract date from transaction created_at
                try {
                    if (transaction.createdAt != null) {
                        // Assuming createdAt is in ISO format, extract date part
                        transaction.createdAt.substring(0, 10)
                    } else {
                        LocalDate.now().format(formatter)
                    }
                } catch (e: Exception) {
                    LocalDate.now().format(formatter)
                }
            }
            .map { (date, dailyTransactions) ->
                val paypalAmount = dailyTransactions
                    .filter { it.paymentMethod.lowercase() == "paypal" }
                    .sumOf { it.amount }
                    
                val cashAmount = dailyTransactions
                    .filter { it.paymentMethod.lowercase() == "tiền mặt" }
                    .sumOf { it.amount }
                    
                DailyRevenue(
                    date = date,
                    paypalAmount = paypalAmount,
                    cashAmount = cashAmount
                )
            }
            .sortedBy { it.date }
    }
    
    private fun groupOrdersByDate(transactions: List<Transaction>): List<DailyOrders> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        return transactions
            .groupBy { transaction ->
                try {
                    if (transaction.createdAt != null) {
                        transaction.createdAt.substring(0, 10)
                    } else {
                        LocalDate.now().format(formatter)
                    }
                } catch (e: Exception) {
                    LocalDate.now().format(formatter)
                }
            }
            .map { (date, dailyTransactions) ->
                DailyOrders(
                    date = date,
                    completedOrders = dailyTransactions.size
                )
            }
            .sortedBy { it.date }
    }
}