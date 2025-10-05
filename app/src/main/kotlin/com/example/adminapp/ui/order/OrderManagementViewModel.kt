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
                
                println("=== DETAILED DEBUG INFO ===")
                println("Booking ID: ${booking.id}")
                println("Customer ID: ${booking.customerId}")
                println("Provider Service ID in booking: ${booking.providerServiceId}")
                println("Available provider services:")
                providerServices.forEach { ps ->
                    println("  - ID: '${ps.id}' (${ps.id.javaClass.simpleName}) | Service ID: ${ps.serviceId} | Provider: ${ps.providerId} -> Service: ${ps.services.name}")
                }
                println("Looking for provider_services WHERE id = '${booking.providerServiceId}' (${booking.providerServiceId.javaClass.simpleName}) (FK constraint)")
                println("Match found: ${providerService != null}")
                println("Found provider service: ${providerService?.services?.name}")
                println("Provider from service: ${provider?.name ?: provider?.email}")
                println("Found customer: ${customer?.name ?: customer?.email}")
                println("Found transaction: ${transaction?.paymentMethod}")
                
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