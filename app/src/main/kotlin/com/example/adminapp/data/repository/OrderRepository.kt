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
                
            println("Đã tải thành công ${result.size} đơn hàng từ cơ sở dữ liệu")
            
            // Debug: In ra tất cả đơn hàng để kiểm tra dữ liệu
            if (result.isNotEmpty()) {
                println("=== DEBUG TẤT CẢ ĐƠN HÀNG ===")
                result.forEachIndexed { index, booking ->
                    println("Đơn hàng ${index + 1}: id=${booking.id}")
                    println("  - ID Khách hàng: ${booking.customerId}")
                    println("  - ID Dịch vụ nhà cung cấp: ${booking.providerServiceId}")
                    println("  - Trạng thái: ${booking.status}")
                    println("  - Vị trí: '${booking.location}'")
                    println("  - Bắt đầu: ${booking.startAt}")
                    println("  - Kết thúc: ${booking.endAt}")
                    println("  - Số công nhân: ${booking.numberWorkers}")
                    println("  - Mô tả: '${booking.description}'")
                    println("  ---")
                }
            }
            
            result
        } catch (e: Exception) {
            println("LỖI: ${e.message}")
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
            println("Lỗi khi tải giao dịch: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCustomers(): List<User> {
        return try {
            println("Đang tải khách hàng từ cơ sở dữ liệu...")
            val result = supabase.from("users")
                .select {
                    filter {
                        eq("role", "customer")
                    }
                }
                .decodeList<User>()
            println("Đã tải thành công ${result.size} khách hàng")
            result
        } catch (e: Exception) {
            println("Lỗi khi tải khách hàng: ${e.message}")
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
            println("Lỗi khi tải nhà cung cấp: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // Data class tạm thời để debug
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
            println("=== TẢI DỊCH VỤ NHÀ CUNG CẤP (ĐƠN GIẢN) ===")
            
            // Bước 1: Lấy dữ liệu thô provider_services
            val rawProviderServices = supabase.from("provider_services")
                .select()
                .decodeList<SimpleProviderService>()
                
            println("Dịch vụ nhà cung cấp thô: ${rawProviderServices.size}")
            rawProviderServices.forEach { ps ->
                println("  - ID: ${ps.id}, ID Dịch vụ: ${ps.service_id}, Nhà cung cấp: ${ps.provider_id}")
            }
            
            // Bước 2: Lấy dịch vụ  
            val services = supabase.from("services")
                .select()
                .decodeList<SimpleService>()
                
            println("Dịch vụ: ${services.size}")
            val serviceMap = services.associateBy { it.id }
            
            // Bước 3: Kết hợp dữ liệu thủ công
            val result = rawProviderServices.mapNotNull { ps ->
                val service = serviceMap[ps.service_id]
                if (service != null) {
                    // Tạo ProviderServiceDetail tối thiểu để test
                    try {
                        ProviderServiceDetail(
                            id = ps.id.toString(),
                            providerId = ps.provider_id,
                            serviceId = ps.service_id.toString(),
                            customPrice = ps.custom_price,
                            services = Service(
                                id = service.id.toString(),
                                name = service.name,
                                serviceTypeId = "1",  // Mặc định tạm thời
                                serviceTypes = ServiceType(
                                    id = 1L,
                                    name = "Default"
                                )
                            )
                        )
                    } catch (e: Exception) {
                        println("Lỗi khi tạo ProviderServiceDetail: ${e.message}")
                        null
                    }
                } else {
                    println("Không tìm thấy dịch vụ cho service_id: ${ps.service_id}")
                    null
                }
            }
            
            println("Kết quả kết hợp: ${result.size}")
            result.forEach { ps ->
                println("Cuối cùng: id=${ps.id}, dịch vụ=${ps.services.name}, nhà cung cấp=${ps.providerId}")
            }
            
            result
            
        } catch (e: Exception) {
            println("LỖI khi tải dịch vụ nhà cung cấp: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProviderServices(providerId: String): List<SimpleProviderService> {
        return try {
            println("=== TẢI DỊCH VỤ NHÀ CUNG CẤP CHO NHÀ CUNG CẤP: $providerId ===")
            
            val result = supabase.from("provider_services")
                .select() {
                    filter {
                        eq("provider_id", providerId)
                    }
                }
                .decodeList<SimpleProviderService>()
                
            println("Tìm thấy ${result.size} dịch vụ nhà cung cấp cho nhà cung cấp $providerId")
            result
        } catch (e: Exception) {
            println("Lỗi khi tải dịch vụ nhà cung cấp: ${e.message}")
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