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
    val customerName: String
)

class OrderListViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    
    var orders by mutableStateOf<List<OrderListItem>>(emptyList())
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
                
                // Tạo danh sách đơn giản chỉ có ID và tên người đặt
                val orderItems = bookings.mapNotNull { booking ->
                    // Skip nếu customerId null
                    val customerId = booking.customerId ?: return@mapNotNull null
                    
                    val customer = customerMap[customerId]
                    val customerName = customer?.name ?: customer?.email ?: "Không rõ"
                    println("Mapping booking ${booking.id} -> customer: $customerName (customerId: $customerId)")
                    
                    OrderListItem(
                        bookingId = booking.id,
                        customerName = customerName
                    )
                }
                
                orders = orderItems.sortedByDescending { it.bookingId }
                println("Final orders list size: ${orders.size}")
                
            } catch (e: Exception) {
                error = "Lỗi khi tải danh sách đơn hàng: ${e.message}"
                println("Error loading orders: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}