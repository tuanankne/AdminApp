package com.example.adminapp.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DailyRevenue(
    val date: String, // Format: yyyy-MM-dd
    val paypalAmount: Double,
    val cashAmount: Double
) {
    val totalRevenue: Double get() = (paypalAmount + cashAmount) * 0.2 // 20% commission
    val paypalRevenue: Double get() = paypalAmount * 0.2
    val cashRevenue: Double get() = cashAmount * 0.2
}

@Serializable  
data class DailyOrders(
    val date: String, // Format: yyyy-MM-dd
    val completedOrders: Int
)

@Serializable
data class TransactionSummary(
    val bookingId: Long,
    val paymentMethod: String,
    val amount: Double,
    val createdAt: String?,
    val revenue: Double = amount * 0.2 // 20% commission
)

data class StatisticsData(
    val dailyRevenues: List<DailyRevenue>,
    val dailyOrders: List<DailyOrders>, 
    val transactionSummaries: List<TransactionSummary>,
    val totalRevenue: Double,
    val totalCompletedOrders: Int
)