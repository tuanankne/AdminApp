package com.example.adminapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Long,
    @SerialName("customer_id") 
    val customerId: String,
    @SerialName("provider_service_id") 
    val providerServiceId: Int,
    val status: String,
    val location: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("start_at") 
    val startAt: String? = null,
    @SerialName("end_at") 
    val endAt: String? = null,
    val description: String? = null,
    @SerialName("number_workers")
    val numberWorkers: Long? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    @SerialName("payment_method")
    val paymentMethod: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("booking_id")
    val bookingId: Long?,
    @SerialName("capture_id")
    val captureId: String?,
    @SerialName("paypal_order_id")
    val paypalOrderId: String?,
    @SerialName("payout_id")
    val payoutId: String?,
    @SerialName("provider_services_id")
    val providerServicesId: Int?
)