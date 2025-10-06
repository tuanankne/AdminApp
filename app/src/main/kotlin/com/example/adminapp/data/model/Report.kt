package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    @SerialName("id")
    val id: Long,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("booking_id")
    val bookingId: Long? = null,
    
    @SerialName("provider_id")
    val providerId: String? = null,
    
    @SerialName("title")
    val title: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("image_urls")
    val imageUrls: String? = null,
    
    @SerialName("status")
    val status: String, // "pending", "completed"
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null,
    
    @SerialName("admin_response")
    val adminResponse: String? = null,
    
    @SerialName("resolved_at")
    val resolvedAt: String? = null
) : java.io.Serializable

// Data class để hiển thị report với thông tin user
@Serializable
data class ReportWithUser(
    val report: Report,
    val customerName: String,
    val customerAvatar: String?,
    val providerName: String? = null
) : java.io.Serializable