package com.example.adminapp.data.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersSignUp(
    val name: String,
    val email: String,
    val password: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val role: String,
    val address: String,
    val lock: String
)

@Serializable
data class UserSignIn(
    val id: String,
    val email: String,
    val name: String,
    val lock: String
)