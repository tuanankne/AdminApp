package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderService(
    @SerialName("id")
    val id: Int,
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("service_id")
    val serviceId: Int,
    @SerialName("custom_price")
    val customPrice: Double? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
