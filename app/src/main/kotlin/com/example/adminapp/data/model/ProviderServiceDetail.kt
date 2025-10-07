package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderServiceDetail(
    @SerialName("id")
    val id: String,
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("service_id")
    val serviceId: String,
    @SerialName("custom_price")
    val customPrice: Double,
    @SerialName("services")
    val services: Service
)

@Serializable
data class Service(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("service_type_id")
    val serviceTypeId: String,
    @SerialName("service_types")
    val serviceTypes: ServiceType
)

@Serializable
data class ServiceType(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
