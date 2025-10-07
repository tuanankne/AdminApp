package com.example.adminapp.ui.service

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.ServiceType
import com.example.adminapp.data.repository.ServiceTypeRepository
import com.example.adminapp.core.supabase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.InputStream

class ServiceManagementViewModel : ViewModel() {
    private val repository = ServiceTypeRepository()
    
    var serviceTypes by mutableStateOf<List<ServiceType>>(emptyList())
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

    init {
        loadServiceTypes()
    }

    fun loadServiceTypes() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                serviceTypes = repository.getServiceTypes()
            } catch (e: Exception) {
                error = "Lỗi tải danh sách dịch vụ: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun toggleServiceTypeStatus(serviceTypeId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            isUpdatingStatus = true
            try {
                val success = repository.updateServiceTypeStatus(serviceTypeId, !currentStatus)
                if (success) {
                    // Update local list
                    serviceTypes = serviceTypes.map { serviceType ->
                        if (serviceType.id == serviceTypeId) {
                            serviceType.copy(isActive = !currentStatus)
                        } else {
                            serviceType
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
    
    fun addServiceType(
        name: String,
        description: String,
        imageInputStream: InputStream?,
        fileName: String?
    ) {
        viewModelScope.launch {
            isAddingService = true
            error = null
            try {
                var iconUrl: String? = null
                
                // Upload image if provided
                if (imageInputStream != null && fileName != null) {
                    isUploadingImage = true
                    iconUrl = repository.uploadImage(imageInputStream, fileName)
                    if (iconUrl == null) {
                        error = "Lỗi tải lên hình ảnh"
                        return@launch
                    }
                }
                
                val success = repository.addServiceType(
                    name = name,
                    description = description,
                    iconUrl = iconUrl,
                    isActive = true
                )
                
                if (success) {
                    loadServiceTypes() // Refresh the list
                } else {
                    error = "Lỗi thêm loại dịch vụ"
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
    
    fun initializeStorageBuckets() {
        viewModelScope.launch {
            try {
                println("=== INITIALIZING STORAGE BUCKETS ===")
                
                // Tạo các bucket cần thiết
                val buckets = listOf(
                    "servicetype" to "Service Type Icons",
                    "report-images" to "Report Images",
                    "user-avatars" to "User Avatars"
                )
                
                buckets.forEach { (bucketId, bucketName) ->
                    try {
                        println("Creating bucket: $bucketId")
                        supabase.storage.createBucket(
                            id = bucketId
                        )
                        println("✅ Bucket '$bucketId' created successfully")
                    } catch (e: Exception) {
                        if (e.message?.contains("already exists") == true) {
                            println("ℹ️ Bucket '$bucketId' already exists")
                        } else {
                            println("❌ Error creating bucket '$bucketId': ${e.message}")
                        }
                    }
                }
                
                println("=== STORAGE BUCKETS INITIALIZATION COMPLETED ===")
                
            } catch (e: Exception) {
                println("❌ Error initializing storage buckets: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun createSampleData() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val success = repository.createSampleServiceTypes()
                if (success) {
                    loadServiceTypes() // Refresh the list
                } else {
                    error = "Lỗi tạo dữ liệu mẫu"
                }
            } catch (e: Exception) {
                error = "Lỗi tạo dữ liệu mẫu: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun updateServiceType(
        serviceTypeId: Long,
        name: String,
        description: String,
        imageInputStream: InputStream?,
        fileName: String?
    ) {
        viewModelScope.launch {
            isAddingService = true
            error = null
            try {
                println("=== UPDATE SERVICE TYPE ===\nID: $serviceTypeId\nName: $name\nDescription: $description")
                
                var iconUrl: String? = null
                
                // Upload new image if provided
                if (imageInputStream != null && fileName != null) {
                    isUploadingImage = true
                    println("Uploading new image...")
                    iconUrl = repository.uploadImage(imageInputStream, fileName)
                    if (iconUrl == null) {
                        error = "Lỗi tải lên hình ảnh"
                        return@launch
                    }
                    println("Image uploaded: $iconUrl")
                }
                
                val success = repository.updateServiceType(
                    serviceTypeId = serviceTypeId,
                    name = name,
                    description = description,
                    iconUrl = iconUrl
                )
                
                println("Update result: $success")
                
                if (success) {
                    loadServiceTypes() // Refresh the list
                } else {
                    error = "Lỗi cập nhật loại dịch vụ"
                }
                
            } catch (e: Exception) {
                error = "Lỗi cập nhật: ${e.message}"
            } finally {
                isAddingService = false
                isUploadingImage = false
            }
        }
    }
    
    fun deleteServiceType(serviceTypeId: Long) {
        viewModelScope.launch {
            isUpdatingStatus = true
            error = null
            try {
                val success = repository.deleteServiceType(serviceTypeId)
                if (success) {
                    // Remove from local list
                    serviceTypes = serviceTypes.filter { it.id != serviceTypeId }
                } else {
                    error = "Lỗi xóa loại dịch vụ"
                }
            } catch (e: Exception) {
                error = "Lỗi xóa: ${e.message}"
            } finally {
                isUpdatingStatus = false
            }
        }
    }
}