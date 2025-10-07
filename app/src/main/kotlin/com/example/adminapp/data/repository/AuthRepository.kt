package com.example.adminapp.data.repository

import android.content.Context
import com.example.adminapp.core.MyFirebaseMessagingService
import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.auth.AuthDtos
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.model.ProviderServiceDetail
import com.example.adminapp.data.model.Service
import com.example.adminapp.data.model.ServiceType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthRepository {

    // Data class tạm thời để deserialize dữ liệu từ Supabase
    @Serializable
    data class ProviderServiceRow(
        val id: Int,
        val provider_id: String,
        val service_id: Int,
        val custom_price: Double
    )

    @Serializable
    data class ServiceRow(
        val id: Int,
        val name: String,
        val service_type_id: Int
    )

    @Serializable
    data class ServiceTypeRow(
        val id: Int,
        val name: String,
        val icon_url: String? = null,
        val is_active: Boolean = true
    )

    suspend fun countAdminUsersByEmail(email: String): Int {
        return try {
            val result = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("role", "admin")
                    }
                }
                .decodeList<AuthDtos.UsersSignUp>()
            if (result.isEmpty()) 0 else 1
        } catch (_: Exception) {
            0
        }
    }

    suspend fun signInAdmin(email: String, password: String): AuthDtos.UserSignIn? {
        return try {
            val users = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("password", password)
                        eq("role", "admin")
                    }
                }
                .decodeList<AuthDtos.UserSignIn>()
            users.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signUpAdmin(user: AuthDtos.UsersSignUp): Boolean {
        return try {
            supabase.from("users").insert(user)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun signOutIfSessionExists() {
        try {
            val currentSession = supabase.auth.currentSessionOrNull()
            if (currentSession != null) {
                supabase.auth.signOut()
            }
        } catch (_: Exception) {
        }
    }

    fun uploadFcmToken(context: Context, userId: String) {
        MyFirebaseMessagingService.generateAndUploadToken(context, userId)
        MyFirebaseMessagingService.uploadPendingToken(context, userId)
    }

    suspend fun fetchProviderById(providerId: String): Provider? {
        return try {
            val providers = supabase.from("users")
                .select {
                    filter {
                        eq("id", providerId)
                        eq("role", "provider")
                    }
                }
                .decodeList<Provider>()
            val provider = providers.firstOrNull()
            
            // Tính toán điểm đánh giá trung bình
            provider?.let { p ->
                val averageRating = calculateProviderAverageRating(providerId)
                p.copy(averageRating = averageRating)
            } ?: provider
        } catch (e: Exception) {
            println("Error fetching provider: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    private suspend fun calculateProviderAverageRating(providerId: String): Double? {
        return try {
            println("=== CALCULATING AVERAGE RATING FOR PROVIDER: $providerId ===")
            
            // 1. Lấy tất cả provider_service_id của nhà cung cấp này
            val providerServices = supabase.from("provider_services")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("provider_id", providerId)
                    }
                }
                .decodeList<Map<String, Int>>()
            
            if (providerServices.isEmpty()) {
                println("Nhà cung cấp không có dịch vụ nào")
                return null
            }
            
            val providerServiceIds = providerServices.map { it["id"]!! }
            println("Found ${providerServiceIds.size} provider services: $providerServiceIds")
            
            // 2. Lấy tất cả đánh giá cho các provider_service_id này
            val ratings = if (providerServiceIds.size == 1) {
                // Nếu chỉ có 1 dịch vụ, dùng eq
                supabase.from("service_ratings")
                    .select(columns = Columns.list("rating")) {
                        filter {
                            eq("provider_service_id", providerServiceIds[0])
                        }
                    }
                    .decodeList<Map<String, Int>>()
            } else {
                // Nếu có nhiều dịch vụ, dùng or với nhiều eq
                supabase.from("service_ratings")
                    .select(columns = Columns.list("rating")) {
                        filter {
                            or {
                                providerServiceIds.forEach { serviceId ->
                                    eq("provider_service_id", serviceId)
                                }
                            }
                        }
                    }
                    .decodeList<Map<String, Int>>()
            }
            
            if (ratings.isEmpty()) {
                println("Không tìm thấy đánh giá nào cho nhà cung cấp này")
                return null
            }
            
            val ratingValues = ratings.map { it["rating"]!! }
            val averageRating = ratingValues.average()
            
            println("Found ${ratingValues.size} ratings: $ratingValues")
            println("Average rating: $averageRating")
            
            averageRating
        } catch (e: Exception) {
            println("Error calculating average rating: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Lấy danh sách dịch vụ mà nhà cung cấp đã đăng ký
     * Logic:
     * 1. users.id (providerId) -> provider_services.provider_id
     * 2. provider_services.service_id -> services.id
     * 3. services.service_type_id -> service_types.id
     */
    suspend fun fetchProviderServicesDetail(providerId: String): List<ProviderServiceDetail> {
        return try {
            println("=== Bắt đầu lấy dịch vụ cho provider: $providerId ===")

            // Bước 1: Lấy tất cả provider_services của provider này
            val providerServices = supabase.from("provider_services")
                .select(columns = Columns.list("id", "provider_id", "service_id", "custom_price")) {
                    filter {
                        eq("provider_id", providerId)
                    }
                }
                .decodeList<ProviderServiceRow>()

            println("Tìm thấy ${providerServices.size} dịch vụ trong provider_services")

            if (providerServices.isEmpty()) {
                println("Provider không có dịch vụ nào")
                return emptyList()
            }

            val result = mutableListOf<ProviderServiceDetail>()

            // Xử lý từng provider_service
            for (ps in providerServices) {
                try {
                    println("\n--- Xử lý provider_service ID: ${ps.id} ---")
                    println("Service ID: ${ps.service_id}, Custom Price: ${ps.custom_price}")

                    // Bước 2: Lấy thông tin service từ service_id
                    val services = supabase.from("services")
                        .select(columns = Columns.list("id", "name", "service_type_id")) {
                            filter {
                                eq("id", ps.service_id)
                            }
                        }
                        .decodeList<ServiceRow>()

                    val service = services.firstOrNull()
                    if (service == null) {
                        println("⚠️ Không tìm thấy service với ID: ${ps.service_id}")
                        continue
                    }

                    println("Service Name: ${service.name}, Service Type ID: ${service.service_type_id}")

                    // Bước 3: Lấy thông tin service_type từ service_type_id
                    val serviceTypes = supabase.from("service_types")
                        .select(columns = Columns.list("id", "name", "icon_url", "is_active")) {
                            filter {
                                eq("id", service.service_type_id)
                            }
                        }
                        .decodeList<ServiceTypeRow>()

                    val serviceType = serviceTypes.firstOrNull()
                    if (serviceType == null) {
                        println("⚠️ Không tìm thấy service_type với ID: ${service.service_type_id}")
                        continue
                    }

                    println("Service Type Name: ${serviceType.name}")

                    // Tạo các đối tượng
                    val serviceTypeObj = ServiceType(
                        id = serviceType.id.toLong(),
                        name = serviceType.name,
                        iconUrl = serviceType.icon_url,
                        isActive = serviceType.is_active
                    )

                    val serviceObj = Service(
                        id = service.id.toString(),
                        name = service.name,
                        serviceTypeId = service.service_type_id.toString(),
                        serviceTypes = serviceTypeObj
                    )

                    val providerServiceDetail = ProviderServiceDetail(
                        id = ps.id.toString(),
                        providerId = ps.provider_id,
                        serviceId = ps.service_id.toString(),
                        customPrice = ps.custom_price,
                        services = serviceObj
                    )

                    result.add(providerServiceDetail)
                    println("✓ Thêm thành công: ${serviceType.name} - ${service.name} - ${ps.custom_price} VND")

                } catch (e: Exception) {
                    println("❌ Lỗi khi xử lý provider_service: ${e.message}")
                    e.printStackTrace()
                }
            }

            println("\n=== Tổng cộng: ${result.size} dịch vụ được tải thành công ===")
            return result

        } catch (e: Exception) {
            println("❌ Lỗi tổng thể trong fetchProviderServicesDetail: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getProviders(): List<Provider> {
        return try {
            supabase.from("users")
                .select {
                    filter {
                        eq("role", "provider")
                    }
                }
                .decodeList<Provider>()
        } catch (e: Exception) {
            println("Lỗi khi tải nhà cung cấp: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCustomers(): List<Provider> {
        return try {
            supabase.from("users")
                .select {
                    filter {
                        eq("role", "customer")
                    }
                }
                .decodeList<Provider>()
        } catch (e: Exception) {
            println("Lỗi khi tải khách hàng: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            supabase.from("users")
                .delete {
                    filter {
                        eq("id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Lỗi khi xóa người dùng: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchCustomerById(customerId: String): Provider? {
        return try {
            val customers = supabase.from("users")
                .select {
                    filter {
                        eq("id", customerId)
                        eq("role", "customer")
                    }
                }
                .decodeList<Provider>()
            customers.firstOrNull()
        } catch (e: Exception) {
            println("Lỗi khi tải khách hàng: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserLockStatus(userId: String, newStatus: String): Boolean {
        return try {
            @Serializable
            data class LockUpdate(
                val lock: String
            )

            supabase.from("users")
                .update(LockUpdate(lock = newStatus)) {
                    filter {
                        eq("id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error updating lock status: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}