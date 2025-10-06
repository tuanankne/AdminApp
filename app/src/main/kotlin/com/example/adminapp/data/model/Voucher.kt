package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Voucher(
    @SerialName("id")
    val id: Long,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("describe")
    val describe: String,
    
    @SerialName("discount")
    val discount: Float,
    
    @SerialName("status")
    val status: String, // "enable" hoặc "disable"
    
    @SerialName("created_at")
    val created_at: String,
    
    @SerialName("date")
    val date: String? = null // Ngày hết hạn
)