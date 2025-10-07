package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val password: String? = null,
    val role: String,
    val name: String? = null,
    val username: String? = null,
    val address: String? = null,
    @SerialName("phone_number")
    val phone_number: String? = null,
    val avatar: String? = null,
    @SerialName("paypal_email")
    val paypal_email: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at") 
    val updatedAt: String? = null
)