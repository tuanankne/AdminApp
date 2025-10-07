package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("lock")
    val lock: String? = "active",
    @SerialName("paypal_email")
    val paypal_email: String? = null,
    @SerialName("average_rating")
    val averageRating: Double? = null
)
