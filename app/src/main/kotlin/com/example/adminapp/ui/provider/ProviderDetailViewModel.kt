package com.example.adminapp.ui.provider

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.model.ProviderServiceDetail
import com.example.adminapp.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProviderDetailViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    var provider by mutableStateOf<Provider?>(null)
        private set
    
    var services by mutableStateOf<List<ProviderServiceDetail>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    var isUpdatingLock by mutableStateOf(false)
        private set

    fun loadProviderDetail(providerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                error = null
                
                // Load provider info
                val providerData = authRepository.fetchProviderById(providerId)
                provider = providerData
                
                // Load provider services
                val servicesData = authRepository.fetchProviderServicesDetail(providerId)
                services = servicesData
                
            } catch (e: Exception) {
                error = "Lỗi khi tải thông tin nhà cung cấp: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleLockStatus() {
        val currentProvider = provider ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                isUpdatingLock = true
                val newStatus = if (currentProvider.lock == "active") "locked" else "active"

                val success = authRepository.updateUserLockStatus(currentProvider.id, newStatus)

                if (success) {
                    // Refresh provider data
                    val updatedProvider = authRepository.fetchProviderById(currentProvider.id)
                    provider = updatedProvider
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

