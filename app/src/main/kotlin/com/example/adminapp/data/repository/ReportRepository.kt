package com.example.adminapp.data.repository

import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.Report
import com.example.adminapp.data.model.ReportWithUser
import com.example.adminapp.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

class ReportRepository {
    
    suspend fun testConnection(): Boolean {
        return try {
            println("=== TESTING REPORTS CONNECTION ===")
            val reports = supabase.from("reports").select().decodeList<Report>()
            println("Table 'reports' accessible, found ${reports.size} records")
            true
        } catch (e: Exception) {
            println("Error accessing reports table: ${e.message}")
            false
        }
    }
    
    suspend fun getReports(): List<ReportWithUser> {
        return try {
            println("=== FETCHING REPORTS ===")
            
            // Lấy tất cả reports
            val reports = supabase.from("reports")
                .select()
                .decodeList<Report>()
            
            println("Found ${reports.size} reports")
            
            if (reports.isEmpty()) {
                println("No reports found in database")
                return emptyList()
            }
            
            // Lấy user IDs từ reports
            val userIds = reports.map { it.userId }.distinct()
            println("Need user info for IDs: $userIds")
            
            // Lấy thông tin users
            val users = supabase.from("users")
                .select(Columns.list("id", "email", "name", "avatar", "role"))
                .decodeList<User>()
            
            println("Found ${users.size} users total")
            
            // Filter customers và providers
            val customers = users.filter { it.role == "customer" }
            val providers = users.filter { it.role == "provider" }
            println("Found ${customers.size} customers and ${providers.size} providers")
            
            // Kết hợp reports với customer và provider info
            val reportsWithUser = reports.map { report ->
                val customer = customers.find { it.id == report.userId }
                val provider = report.providerId?.let { providerId ->
                    providers.find { it.id == providerId }
                }
                ReportWithUser(
                    report = report,
                    customerName = customer?.name ?: "Unknown User",
                    customerAvatar = customer?.avatar,
                    providerName = provider?.name
                )
            }
            
            println("Processed ${reportsWithUser.size} reports with user info")
            
            // Sort: pending reports first, then completed reports
            reportsWithUser.sortedWith { a, b ->
                when {
                    a.report.status == "pending" && b.report.status != "pending" -> -1
                    a.report.status != "pending" && b.report.status == "pending" -> 1
                    else -> b.report.id.compareTo(a.report.id) // Newest first within same status
                }
            }
            
        } catch (e: Exception) {
            println("Error fetching reports: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    @Serializable
    data class ReportUpdateData(
        val status: String,
        @SerialName("admin_response") val adminResponse: String? = null,
        @SerialName("resolved_at") val resolvedAt: String? = null
    )
    
    suspend fun updateReportStatus(id: Long, status: String, adminResponse: String? = null): Boolean {
        return try {
            println("Updating report $id to status: $status")
            
            val currentTime = getCurrentTimestamp()
            val updateData = if (adminResponse != null) {
                ReportUpdateData(
                    status = status,
                    adminResponse = adminResponse,
                    resolvedAt = currentTime
                )
            } else {
                ReportUpdateData(
                    status = status
                )
            }
            
            supabase.from("reports")
                .update(updateData) {
                    filter {
                        eq("id", id)
                    }
                }
            
            println("Successfully updated report status")
            true
            
        } catch (e: Exception) {
            println("Error updating report: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun deleteReport(id: Long): Boolean {
        return try {
            println("Deleting report: $id")
            supabase.from("reports").delete()
            println("Successfully deleted report")
            true
        } catch (e: Exception) {
            println("Error deleting report: ${e.message}")
            false
        }
    }
    
    suspend fun getPendingReportsCount(): Int {
        return try {
            val reports = supabase.from("reports")
                .select() {
                    filter {
                        eq("status", "pending")
                    }
                }
                .decodeList<Report>()
            reports.size
        } catch (e: Exception) {
            println("Error getting pending reports count: ${e.message}")
            0
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}