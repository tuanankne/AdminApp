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
                
                println("=== Bắt đầu tải đơn hàng ===")
                
                // Tải đơn hàng và khách hàng
                val bookings = orderRepository.getBookings()
                println("Đã tải ${bookings.size} đơn hàng")
                
                val customers = orderRepository.getCustomers()
                println("Đã tải ${customers.size} khách hàng")
                
                // Debug: In ra vài đơn hàng đầu tiên
                bookings.take(3).forEach { booking ->
                    println("Đơn hàng: id=${booking.id}, customerId=${booking.customerId}")
                }
                
                // Tạo map để tra cứu khách hàng
                val customerMap = customers.associateBy { it.id }
                println("Map khách hàng có ${customerMap.size} mục")
                
                // Tạo danh sách đơn giản chỉ có ID, tên người đặt và trạng thái
                val orderItems = bookings.mapNotNull { booking ->
                    // Bỏ qua nếu customerId null
                    val customerId = booking.customerId ?: return@mapNotNull null
                    
                    val customer = customerMap[customerId]
                    val customerName = customer?.name ?: customer?.email ?: "Không rõ"
                    println("Ánh xạ đơn hàng ${booking.id} -> khách hàng: $customerName (customerId: $customerId), trạng thái: ${booking.status}")
                    
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
                
                println("Kích thước danh sách đơn hàng cuối cùng: ${orders.size}")
                println("Thống kê: Tổng=$totalOrders, Hoàn thành=$completedOrders")
                
            } catch (e: Exception) {
                error = "Lỗi khi tải danh sách đơn hàng: ${e.message}"
                println("Lỗi khi tải đơn hàng: ${e.message}")
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
                
                println("=== Bắt đầu tải đơn hàng cho nhà cung cấp: $providerId ===")
                
                // Tải đơn hàng và khách hàng
                val bookings = orderRepository.getBookings()
                println("Đã tải ${bookings.size} đơn hàng")
                
                val customers = orderRepository.getCustomers()
                println("Đã tải ${customers.size} khách hàng")
                
                // Lấy dịch vụ nhà cung cấp để lọc đơn hàng
                val providerServices = orderRepository.getProviderServices(providerId)
                println("Tìm thấy ${providerServices.size} dịch vụ nhà cung cấp cho nhà cung cấp $providerId")
                
                val providerServiceIds = providerServices.map { it.id }
                
                // Tạo map để tra cứu khách hàng
                val customerMap = customers.associateBy { it.id }
                
                // Lọc đơn hàng theo ID dịch vụ nhà cung cấp
                val filteredBookings = bookings.filter { booking ->
                    providerServiceIds.contains(booking.providerServiceId)
                }
                
                println("Đã lọc thành ${filteredBookings.size} đơn hàng cho nhà cung cấp này")
                
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
                
                println("Kích thước danh sách đơn hàng đã lọc cuối cùng: ${orders.size}")
                println("Thống kê: Tổng=$totalOrders, Hoàn thành=$completedOrders")
                
            } catch (e: Exception) {
                error = "Lỗi khi tải đơn hàng: ${e.message}"
                println("Lỗi khi tải đơn hàng theo nhà cung cấp: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}