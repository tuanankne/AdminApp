package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceRating(
    @SerialName("id")
    val id: Long,
    @SerialName("provider_service_id")
    val providerServiceId: Int,
    @SerialName("user_id")
    val userId: String,
    @SerialName("rating")
    val rating: Int,
    @SerialName("comment")
    val comment: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("responses")
    val responses: String? = null,
    @SerialName("booking_id")
    val bookingId: Long? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
