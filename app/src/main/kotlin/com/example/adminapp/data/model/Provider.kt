package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val avatar: String? = null
)

@Serializable
data class ProviderService(
    val id: String,
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("service_id")
    val serviceId: String,
    @SerialName("custom_price")
    val customPrice: Double
)

@Serializable
data class Service(
    val id: String,
    val name: String,
    @SerialName("service_type_id")
    val serviceTypeId: String
)

@Serializable
data class ServiceType(
    val id: String,
    val name: String
)

@Serializable
data class ProviderServiceDetail(
    val serviceTypeName: String,
    val serviceName: String,
    val customPrice: Double
)
