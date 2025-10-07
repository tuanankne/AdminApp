package com.example.adminapp.ui.order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class OrderListItem(
    val bookingId: Long,
    val customerName: String,
    val status: String
)

data class OrderStatistics(
    val totalOrders: Int,
    val completedOrders: Int
)

class OrderListViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    var orders by mutableStateOf<List<OrderListItem>>(emptyList())
        private set
    
    var statistics by mutableStateOf(OrderStatistics(0, 0))
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    fun loadOrders() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                error = null
                
                println("=== Starting to load orders ===")
                
                // Load bookings và customers
                val bookings = orderRepository.getBookings()
                println("Loaded ${bookings.size} bookings")
                
                val customers = orderRepository.getCustomers()
                println("Loaded ${customers.size} customers")
                
                // Debug: Print first few bookings
                bookings.take(3).forEach { booking ->
                    println("Booking: id=${booking.id}, customerId=${booking.customerId}")
                }
                
                // Tạo map để lookup customer
                val customerMap = customers.associateBy { it.id }
                println("Customer map has ${customerMap.size} entries")
                
                // Tạo danh sách đơn giản chỉ có ID, tên người đặt và trạng thái
                val orderItems = bookings.mapNotNull { booking ->
                    // Skip nếu customerId null
                    val customerId = booking.customerId ?: return@mapNotNull null
                    
                    val customer = customerMap[customerId]
                    val customerName = customer?.name ?: customer?.email ?: "Không rõ"
                    println("Mapping booking ${booking.id} -> customer: $customerName (customerId: $customerId), status: ${booking.status}")
                    
                    OrderListItem(
                        bookingId = booking.id,
                        customerName = customerName,
                        status = booking.status
                    )
                }
                
                orders = orderItems.sortedByDescending { it.bookingId }
                
                // Tính toán thống kê
                val totalOrders = orderItems.size
                val completedOrders = orderItems.count { it.status == "completed" }
                statistics = OrderStatistics(totalOrders, completedOrders)
                
                println("Final orders list size: ${orders.size}")
                println("Statistics: Total=$totalOrders, Completed=$completedOrders")
                
            } catch (e: Exception) {
                error = "Lỗi khi tải danh sách đơn hàng: ${e.message}"
                println("Error loading orders: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    fun loadOrdersByProvider(providerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                error = null
                
                println("=== Starting to load orders for provider: $providerId ===")
                
                // Load bookings và customers
                val bookings = orderRepository.getBookings()
                println("Loaded ${bookings.size} bookings")
                
                val customers = orderRepository.getCustomers()
                println("Loaded ${customers.size} customers")
                
                // Lấy provider services để filter bookings
                val providerServices = orderRepository.getProviderServices(providerId)
                println("Found ${providerServices.size} provider services for provider $providerId")
                
                val providerServiceIds = providerServices.map { it.id }
                
                // Tạo map để lookup customer
                val customerMap = customers.associateBy { it.id }
                
                // Filter bookings theo provider service IDs
                val filteredBookings = bookings.filter { booking ->
                    providerServiceIds.contains(booking.providerServiceId)
                }
                
                println("Filtered to ${filteredBookings.size} bookings for this provider")
                
                // Tạo danh sách đơn giản chỉ có ID, tên người đặt và trạng thái
                val orderItems = filteredBookings.mapNotNull { booking ->
                    val customerId = booking.customerId ?: return@mapNotNull null
                    
                    val customer = customerMap[customerId]
                    val customerName = customer?.name ?: customer?.email ?: "Không rõ"
                    
                    OrderListItem(
                        bookingId = booking.id,
                        customerName = customerName,
                        status = booking.status
                    )
                }
                
                orders = orderItems.sortedByDescending { it.bookingId }
                
                // Tính toán thống kê
                val totalOrders = orderItems.size
                val completedOrders = orderItems.count { it.status == "completed" }
                statistics = OrderStatistics(totalOrders, completedOrders)
                
                println("Final filtered orders list size: ${orders.size}")
                println("Statistics: Total=$totalOrders, Completed=$completedOrders")
                
            } catch (e: Exception) {
                error = "Lỗi khi tải đơn hàng: ${e.message}"
                println("Error loading orders by provider: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}