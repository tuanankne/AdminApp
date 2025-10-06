package com.example.adminapp.ui.service

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.ServiceDetail
import com.example.adminapp.data.repository.ServiceDetailRepository
import kotlinx.coroutines.launch
import java.io.InputStream

class ServiceDetailViewModel : ViewModel() {
    private val repository = ServiceDetailRepository()
    
    var services by mutableStateOf<List<ServiceDetail>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
        
    var error by mutableStateOf<String?>(null)
        private set
        
    var isUpdatingStatus by mutableStateOf(false)
        private set
        
    var isAddingService by mutableStateOf(false)
        private set
        
    var isUploadingImage by mutableStateOf(false)
        private set
        
    var serviceTypeId by mutableStateOf(0L)
        private set

    fun loadServices(typeId: Long) {
        serviceTypeId = typeId
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                services = repository.getServicesByType(typeId)
            } catch (e: Exception) {
                error = "Lỗi tải danh sách dịch vụ: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun toggleServiceStatus(serviceId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            isUpdatingStatus = true
            try {
                val success = repository.updateServiceStatus(serviceId, !currentStatus)
                if (success) {
                    // Update local list
                    services = services.map { service ->
                        if (service.id == serviceId) {
                            service.copy(isActive = !currentStatus)
                        } else {
                            service
                        }
                    }
                } else {
                    error = "Lỗi cập nhật trạng thái dịch vụ"
                }
            } catch (e: Exception) {
                error = "Lỗi cập nhật trạng thái: ${e.message}"
            } finally {
                isUpdatingStatus = false
            }
        }
    }
    
    fun addService(
        name: String,
        description: String,
        durationMinute: Int,
        imageInputStream: InputStream?,
        fileName: String?
    ) {
        viewModelScope.launch {
            isAddingService = true
            error = null
            try {
                var imageUrl: String? = null
                
                // Upload image if provided
                if (imageInputStream != null && fileName != null) {
                    isUploadingImage = true
                    imageUrl = repository.uploadImage(imageInputStream, fileName)
                    if (imageUrl == null) {
                        error = "Lỗi tải lên hình ảnh"
                        return@launch
                    }
                }
                
                val success = repository.addService(
                    name = name,
                    description = description,
                    durationMinutes = durationMinute,
                    serviceTypeId = serviceTypeId,
                    imageUrl = imageUrl
                )
                
                if (success) {
                    loadServices(serviceTypeId) // Refresh the list
                } else {
                    error = "Lỗi thêm dịch vụ"
                }
                
            } catch (e: Exception) {
                error = "Lỗi thêm dịch vụ: ${e.message}"
            } finally {
                isAddingService = false
                isUploadingImage = false
            }
        }
    }
    
    fun clearError() {
        error = null
    }
    
    fun updateService(
        serviceId: Long,
        name: String? = null,
        description: String? = null,
        durationMinute: Int? = null,
        imageInputStream: InputStream? = null,
        fileName: String? = null
    ) {
        viewModelScope.launch {
            isAddingService = true
            error = null
            try {
                var imageUrl: String? = null
                
                // Upload new image if provided
                if (imageInputStream != null && fileName != null) {
                    isUploadingImage = true
                    imageUrl = repository.uploadImage(imageInputStream, fileName)
                    if (imageUrl == null) {
                        error = "Lỗi tải lên hình ảnh"
                        return@launch
                    }
                } else {
                    // Keep existing image URL if no new image
                    imageUrl = services.find { it.id == serviceId }?.imageUrl
                }
                
                val success = repository.updateService(
                    serviceId = serviceId,
                    name = name,
                    description = description,
                    durationMinutes = durationMinute,
                    imageUrl = imageUrl
                )
                
                if (success) {
                    loadServices(serviceTypeId) // Refresh the list
                } else {
                    error = "Lỗi cập nhật dịch vụ"
                }
                
            } catch (e: Exception) {
                error = "Lỗi cập nhật: ${e.message}"
            } finally {
                isAddingService = false
                isUploadingImage = false
            }
        }
    }
    
    fun deleteService(serviceId: Long) {
        viewModelScope.launch {
            isUpdatingStatus = true
            error = null
            try {
                val success = repository.deleteService(serviceId)
                if (success) {
                    // Remove from local list
                    services = services.filter { it.id != serviceId }
                } else {
                    error = "Lỗi xóa dịch vụ"
                }
            } catch (e: Exception) {
                error = "Lỗi xóa: ${e.message}"
            } finally {
                isUpdatingStatus = false
            }
        }
    }
}