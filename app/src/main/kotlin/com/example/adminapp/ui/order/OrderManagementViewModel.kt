package com.example.adminapp.ui.order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.Booking
import com.example.adminapp.data.model.Transaction
import com.example.adminapp.data.model.User
import com.example.adminapp.data.model.ProviderServiceDetail
import com.example.adminapp.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class OrderDetail(
    val booking: Booking,
    val customer: User?,
    val provider: User?,
    val providerService: ProviderServiceDetail?,
    val transaction: Transaction?
)

class OrderManagementViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    var orders by mutableStateOf<List<OrderDetail>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                error = null
                
                // Load chỉ đơn hàng cần thiết
                val bookings = orderRepository.getBookings()
                val booking = bookings.find { it.id == orderId }
                
                if (booking == null) {
                    error = "Không tìm thấy đơn hàng #$orderId"
                    orders = emptyList()
                    return@launch
                }
                
                // Load dữ liệu liên quan
                val transactions = orderRepository.getTransactions()
                val customers = orderRepository.getCustomers()
                val providers = orderRepository.getProviders()
                val providerServices = orderRepository.getProviderServices()
                
                // Tạo maps để lookup nhanh  
                val transactionMap = transactions.associateBy { it.bookingId }
                val customerMap = customers.associateBy { it.id }
                
                // CORRECT mapping: bookings.provider_service_id -> provider_services.id (FK constraint)
                // Tìm provider_service có id = booking.providerServiceId
                // Try both String and Int comparison
                val providerService = providerServices.find { 
                    it.id == booking.providerServiceId.toString() || it.id.toIntOrNull() == booking.providerServiceId 
                }
                
                val transaction = transactionMap[booking.id]
                val customer = customerMap[booking.customerId]
                
                // Tìm provider từ provider_service.provider_id
                val providerMap = providers.associateBy { it.id }
                val provider = providerService?.let { service ->
                    providerMap[service.providerId]
                }
                
                println("=== THÔNG TIN DEBUG CHI TIẾT ===")
                println("ID Đơn hàng: ${booking.id}")
                println("ID Khách hàng: ${booking.customerId}")
                println("ID Dịch vụ nhà cung cấp trong đơn hàng: ${booking.providerServiceId}")
                println("Dịch vụ nhà cung cấp có sẵn:")
                providerServices.forEach { ps ->
                    println("  - ID: '${ps.id}' (${ps.id.javaClass.simpleName}) | ID Dịch vụ: ${ps.serviceId} | Nhà cung cấp: ${ps.providerId} -> Dịch vụ: ${ps.services.name}")
                }
                println("Tìm kiếm provider_services WHERE id = '${booking.providerServiceId}' (${booking.providerServiceId.javaClass.simpleName}) (ràng buộc FK)")
                println("Tìm thấy khớp: ${providerService != null}")
                println("Dịch vụ nhà cung cấp tìm thấy: ${providerService?.services?.name}")
                println("Nhà cung cấp từ dịch vụ: ${provider?.name ?: provider?.email}")
                println("Khách hàng tìm thấy: ${customer?.name ?: customer?.email}")
                println("Giao dịch tìm thấy: ${transaction?.paymentMethod}")
                
                val orderDetail = OrderDetail(
                    booking = booking,
                    customer = customer,
                    provider = provider,
                    providerService = providerService,
                    transaction = transaction
                )
                
                orders = listOf(orderDetail)
                
            } catch (e: Exception) {
                error = "Lỗi khi tải chi tiết đơn hàng: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                
                val success = orderRepository.cancelOrder(orderId)
                if (success) {
                    // Reload order details to update UI
                    loadOrderDetail(orderId)
                } else {
                    error = "Không thể hủy đơn hàng"
                }
            } catch (e: Exception) {
                error = "Lỗi khi hủy đơn hàng: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}