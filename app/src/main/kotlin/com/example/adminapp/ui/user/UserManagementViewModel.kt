package com.example.adminapp.ui.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserManagementViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    var providers by mutableStateOf<List<Provider>>(emptyList())
        private set
    
    var customers by mutableStateOf<List<Provider>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                error = null
                
                // Load providers
                val providersData = authRepository.getProviders()
                providers = providersData
                
                // Load customers
                val customersData = authRepository.getCustomers()
                customers = customersData
                
            } catch (e: Exception) {
                error = "Lỗi khi tải danh sách người dùng: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = authRepository.deleteUser(providerId)
                if (success) {
                    // Reload data
                    loadUsers()
                } else {
                    error = "Không thể xóa nhà cung cấp"
                }
            } catch (e: Exception) {
                error = "Lỗi khi xóa nhà cung cấp: ${e.message}"
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = authRepository.deleteUser(customerId)
                if (success) {
                    // Reload data
                    loadUsers()
                } else {
                    error = "Không thể xóa khách hàng"
                }
            } catch (e: Exception) {
                error = "Lỗi khi xóa khách hàng: ${e.message}"
            }
        }
    }
}