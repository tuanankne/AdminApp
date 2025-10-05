package com.example.adminapp.data.repository

import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.Booking
import com.example.adminapp.data.model.Transaction
import com.example.adminapp.data.model.User
import com.example.adminapp.data.model.ProviderServiceDetail
import com.example.adminapp.data.model.Service
import com.example.adminapp.data.model.ServiceType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

data class OrderDetailData(
    val booking: Booking,
    val customer: User?,
    val provider: User?,
    val providerService: ProviderServiceDetail?,
    val transaction: Transaction?
)

class OrderRepository {
    
    suspend fun getBookings(): List<Booking> {
        return try {
            println("=== FETCHING BOOKINGS ===")
            
            val result = supabase.from("bookings")
                .select()
                .decodeList<Booking>()
                
            println("Successfully fetched ${result.size} bookings from database")
            
            // Debug: In ra tất cả booking để kiểm tra data
            if (result.isNotEmpty()) {
                println("=== ALL BOOKINGS DEBUG ===")
                result.forEachIndexed { index, booking ->
                    println("Booking ${index + 1}: id=${booking.id}")
                    println("  - Customer ID: ${booking.customerId}")
                    println("  - Provider Service ID: ${booking.providerServiceId}")
                    println("  - Status: ${booking.status}")
                    println("  - Location: '${booking.location}'")
                    println("  - Start: ${booking.startAt}")
                    println("  - End: ${booking.endAt}")
                    println("  - Workers: ${booking.numberWorkers}")
                    println("  - Description: '${booking.description}'")
                    println("  ---")
                }
            }
            
            result
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTransactions(): List<Transaction> {
        return try {
            supabase.from("transactions")
                .select()
                .decodeList<Transaction>()
        } catch (e: Exception) {
            println("Error fetching transactions: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCustomers(): List<User> {
        return try {
            println("Fetching customers from database...")
            val result = supabase.from("users")
                .select {
                    filter {
                        eq("role", "customer")
                    }
                }
                .decodeList<User>()
            println("Successfully fetched ${result.size} customers")
            result
        } catch (e: Exception) {
            println("Error fetching customers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProviders(): List<User> {
        return try {
            supabase.from("users")
                .select {
                    filter {
                        eq("role", "provider")
                    }
                }
                .decodeList<User>()
        } catch (e: Exception) {
            println("Error fetching providers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // Temporary simple data classes for debugging
    @kotlinx.serialization.Serializable
    data class SimpleProviderService(
        val id: Int,
        val provider_id: String,
        val service_id: Int,
        val custom_price: Double
    )
    
    @kotlinx.serialization.Serializable  
    data class SimpleService(
        val id: Int,
        val name: String
    )

    suspend fun getProviderServices(): List<ProviderServiceDetail> {
        return try {
            println("=== FETCHING PROVIDER SERVICES (SIMPLE) ===")
            
            // Step 1: Get raw provider_services
            val rawProviderServices = supabase.from("provider_services")
                .select()
                .decodeList<SimpleProviderService>()
                
            println("Raw provider services: ${rawProviderServices.size}")
            rawProviderServices.forEach { ps ->
                println("  - ID: ${ps.id}, Service ID: ${ps.service_id}, Provider: ${ps.provider_id}")
            }
            
            // Step 2: Get services  
            val services = supabase.from("services")
                .select()
                .decodeList<SimpleService>()
                
            println("Services: ${services.size}")
            val serviceMap = services.associateBy { it.id }
            
            // Step 3: Manually combine data
            val result = rawProviderServices.mapNotNull { ps ->
                val service = serviceMap[ps.service_id]
                if (service != null) {
                    // Create a minimal ProviderServiceDetail for testing
                    try {
                        ProviderServiceDetail(
                            id = ps.id.toString(),
                            providerId = ps.provider_id,
                            serviceId = ps.service_id.toString(),
                            customPrice = ps.custom_price,
                            services = Service(
                                id = service.id.toString(),
                                name = service.name,
                                serviceTypeId = "1",  // Default for now
                                serviceTypes = ServiceType(
                                    id = "1",
                                    name = "Default"
                                )
                            )
                        )
                    } catch (e: Exception) {
                        println("Error creating ProviderServiceDetail: ${e.message}")
                        null
                    }
                } else {
                    println("No service found for service_id: ${ps.service_id}")
                    null
                }
            }
            
            println("Combined result: ${result.size}")
            result.forEach { ps ->
                println("Final: id=${ps.id}, service=${ps.services.name}, provider=${ps.providerId}")
            }
            
            result
            
        } catch (e: Exception) {
            println("ERROR fetching provider services: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProviderByServiceId(serviceId: String): User? {
        return try {
            println("=== FETCHING PROVIDER BY SERVICE ID: $serviceId ===")
            
            // Lấy provider_id từ provider_services table dựa trên service_id
            val providerService = supabase.from("provider_services")
                .select(columns = Columns.list("provider_id")) {
                    filter {
                        eq("service_id", serviceId)  // Fix: query by service_id, not id
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()

            val providerId = providerService?.get("provider_id")
            println("Found provider_id: $providerId for service_id: $serviceId")
            
            if (providerId == null) return null

            // Lấy thông tin provider
            val provider = supabase.from("users")
                .select {
                    filter {
                        eq("id", providerId)
                        eq("role", "provider")
                    }
                }
                .decodeSingleOrNull<User>()
                
            println("Found provider: ${provider?.name ?: provider?.email} for provider_id: $providerId")
            provider
        } catch (e: Exception) {
            println("Error fetching provider by service id: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    suspend fun cancelOrder(orderId: Long): Boolean {
        return try {
            println("=== CANCELLING ORDER $orderId ===")
            
            supabase.from("bookings")
                .update({
                    set("status", "cancelled")
                }) {
                    filter {
                        eq("id", orderId)
                    }
                }
            
            println("Order $orderId cancelled successfully")
            true
        } catch (e: Exception) {
            println("ERROR cancelling order: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}