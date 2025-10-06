package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDetail(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("service_type_id")
    val serviceTypeId: Long,
    @SerialName("is_active")
    val isActive: Boolean = true
)