package com.example.adminapp.data.repository

import android.content.Context
import com.example.adminapp.core.MyFirebaseMessagingService
import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.auth.UsersSignUp
import com.example.adminapp.data.model.auth.UserSignIn
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.model.ProviderService
import com.example.adminapp.data.model.Service
import com.example.adminapp.data.model.ServiceType
import com.example.adminapp.data.model.ProviderServiceDetail
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class AuthRepository {

    suspend fun countAdminUsersByEmail(email: String): Int {
        return try {
            val result = supabase.from("users")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("email", email)
                        eq("role", "admin")
                    }
                }
                .decodeList<Map<String, String>>()
            if (result.isEmpty()) 0 else 1
        } catch (_: Exception) {
            0
        }
    }

    suspend fun signInAdmin(email: String, password: String): UserSignIn? {
        return try {
            val users = supabase.from("users")
                .select(columns = Columns.list("email", "password", "id", "name", "lock")) {
                    filter {
                        eq("email", email)
                        eq("password", password)
                        eq("role", "admin")
                        eq("lock", "active")
                    }
                }
                .decodeList<UserSignIn>()
            users.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signUpAdmin(user: UsersSignUp): Boolean {
        return try {
            val response = supabase.from("users").insert(user)
            println("Insert user response: $response")
            true
        } catch (e: Exception) {
            println("Exception when inserting user: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun signOutIfSessionExists() {
        try {
            val currentSession = supabase.auth.currentSessionOrNull()
            if (currentSession != null) {
                supabase.auth.signOut()
            }
        } catch (e: Exception) {
            println("Error signing out: ${e.message}")
        }
    }

    fun uploadFcmToken(context: Context, userId: String) {
        MyFirebaseMessagingService.generateAndUploadToken(context, userId)
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            supabase.from("users").delete {
                filter {
                    eq("id", userId)
                }
            }
            true
        } catch (e: Exception) {
            println("Error deleting user: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun getProviders(): List<Provider> {
        return try {
            val providers = supabase.from("users")
                .select(columns = Columns.list("id", "name", "email", "password", "address", "phone_number", "avatar")) {
                    filter {
                        eq("role", "provider")
                    }
                }
                .decodeList<Provider>()
            providers
        } catch (e: Exception) {
            println("Error getting providers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCustomers(): List<Provider> {
        return try {
            val customers = supabase.from("users")
                .select(columns = Columns.list("id", "name", "email", "password", "address", "phone_number", "avatar")) {
                    filter {
                        eq("role", "customer")
                    }
                }
                .decodeList<Provider>()
            customers
        } catch (e: Exception) {
            println("Error getting customers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProviderById(providerId: String): Provider? {
        return try {
            val providers = supabase.from("users")
                .select(columns = Columns.list("id", "name", "email", "password", "address", "phone_number", "avatar")) {
                    filter {
                        eq("id", providerId)
                        eq("role", "provider")
                    }
                }
                .decodeList<Provider>()
            providers.firstOrNull()
        } catch (e: Exception) {
            println("Error getting provider: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getProviderServicesDetail(providerId: String): List<ProviderServiceDetail> {
        return try {
            // Lấy provider_services
            val providerServices = supabase.from("provider_services")
                .select(columns = Columns.list("service_id", "custom_price")) {
                    filter {
                        eq("provider_id", providerId)
                    }
                }
                .decodeList<ProviderService>()

            if (providerServices.isEmpty()) {
                return emptyList()
            }

            // Lấy serviceIds
            val serviceIds = providerServices.map { it.serviceId }

            // Lấy services
            val services = supabase.from("services")
                .select(columns = Columns.list("id", "name", "service_type_id")) {
                    filter {
                        eq("id", serviceIds.firstOrNull() ?: "")
                    }
                }
                .decodeList<Service>()

            // Lấy serviceTypeIds
            val serviceTypeIds = services.map { it.serviceTypeId }.distinct()

            // Lấy service_types
            val serviceTypes = supabase.from("service_types")
                .select(columns = Columns.list("id", "name")) {
                    filter {
                        eq("id", serviceTypeIds.firstOrNull() ?: "")
                    }
                }
                .decodeList<ServiceType>()

            // Map dữ liệu
            providerServices.mapNotNull { ps ->
                val service = services.find { it.id == ps.serviceId }
                val serviceType = service?.let { serviceTypes.find { st -> st.id == it.serviceTypeId } }
                
                if (service != null && serviceType != null) {
                    ProviderServiceDetail(
                        serviceTypeName = serviceType.name,
                        serviceName = service.name,
                        customPrice = ps.customPrice
                    )
                } else null
            }
        } catch (e: Exception) {
            println("Error getting provider services: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}