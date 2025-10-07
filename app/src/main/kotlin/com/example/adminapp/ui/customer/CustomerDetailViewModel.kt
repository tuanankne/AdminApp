package com.example.adminapp.ui.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomerDetailViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var customer by mutableStateOf<Provider?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var isUpdatingLock by mutableStateOf(false)
        private set

    fun loadCustomerDetail(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                error = null

                // Tải thông tin khách hàng
                val customerData = authRepository.fetchCustomerById(customerId)
                customer = customerData

            } catch (e: Exception) {
                error = "Lỗi khi tải thông tin khách hàng: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleLockStatus() {
        val currentCustomer = customer ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                isUpdatingLock = true
                val newStatus = if (currentCustomer.lock == "active") "locked" else "active"

                val success = authRepository.updateUserLockStatus(currentCustomer.id, newStatus)

                if (success) {
                    // Làm mới dữ liệu khách hàng
                    val updatedCustomer = authRepository.fetchCustomerById(currentCustomer.id)
                    customer = updatedCustomer
                } else {
                    error = "Không thể cập nhật trạng thái khóa"
                }
            } catch (e: Exception) {
                error = "Lỗi khi cập nhật trạng thái: ${e.message}"
            } finally {
                isUpdatingLock = false
            }
        }
    }
}

