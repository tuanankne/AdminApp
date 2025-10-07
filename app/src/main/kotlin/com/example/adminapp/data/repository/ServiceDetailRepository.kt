package com.example.adminapp.data.repository

import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.ServiceDetail
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ServiceDetailRepository {

    private suspend fun getMaxServiceId(): Long {
        return try {
            // Lấy tất cả services và tìm max ID
            val services = supabase.from("services")
                .select()
                .decodeList<ServiceDetail>()
            
            val maxId = services.maxOfOrNull { it.id } ?: 0L
            println("Max Service ID found: $maxId")
            maxId
        } catch (e: Exception) {
            println("Error getting max service ID, using 0: ${e.message}")
            0L
        }
    }

    suspend fun getServicesByType(serviceTypeId: Long): List<ServiceDetail> {
        return try {
            println("=== TẢI DỊCH VỤ THEO ID LOẠI: $serviceTypeId ===")
            
            val result = supabase.from("services")
                .select {
                    filter {
                        eq("service_type_id", serviceTypeId)
                    }
                }
                .decodeList<ServiceDetail>()
                
            println("Đã tải thành công ${result.size} dịch vụ")
            
            // Debug: In ra từng dịch vụ
            result.forEachIndexed { index, service ->
                println("Dịch vụ ${index + 1}: id=${service.id}, tên='${service.name}', mô tả='${service.description}', thời gian=${service.durationMinutes}phút")
            }
            
            result
            
        } catch (e: Exception) {
            println("Lỗi khi tải dịch vụ: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun updateServiceStatus(serviceId: Long, isActive: Boolean): Boolean {
        return try {
            println("=== UPDATING SERVICE STATUS ===")
            println("Service ID: $serviceId, Is Active: $isActive")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            supabase.from("services")
                .update(
                    mapOf(
                        "is_active" to isActive.toString(),
                        "updated_at" to currentTime
                    )
                ) {
                    filter {
                        eq("id", serviceId)
                    }
                }
            
            println("Successfully updated service status")
            true
            
        } catch (e: Exception) {
            println("Error updating service status: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun addService(
        name: String,
        description: String,
        durationMinutes: Int,
        serviceTypeId: Long,
        imageUrl: String? = null
    ): Boolean {
        return try {
            println("=== ADDING NEW SERVICE ===")
            println("Name: $name, Description: $description, Duration: ${durationMinutes}min, TypeID: $serviceTypeId")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            // Lấy ID lớn nhất hiện có + 1
            val maxId = getMaxServiceId()
            val id = maxId + 1
            
            val insertData = mutableMapOf<String, String>(
                "id" to id.toString(),                     // ID tự động tạo
                "name" to name,
                "description" to description,
                "duration_minutes" to durationMinutes.toString(),
                "service_type_id" to serviceTypeId.toString(),
                "is_active" to "true",
                "created_at" to currentTime,    // Thời gian tạo = thời gian hiện tại
                "updated_at" to currentTime      // Thời gian cập nhật = thời gian hiện tại (khi tạo mới)
            )
            
            imageUrl?.let { insertData["image_url"] = it }
            
            supabase.from("services")
                .insert(insertData)
            
            println("Successfully added new service")
            true
            
        } catch (e: Exception) {
            println("Error adding service: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun uploadImage(inputStream: InputStream, fileName: String): String? {
        return try {
            println("=== UPLOADING SERVICE IMAGE ===")
            println("File name: $fileName")
            
            // Tạo tên file với UUID để tránh trùng lặp
            val uuid1 = java.util.UUID.randomUUID().toString()
            val uuid2 = java.util.UUID.randomUUID().toString()
            val fileExtension = fileName.substringAfterLast(".", "jpg")
            val newFileName = "${uuid1}_${uuid2}.${fileExtension}"
            
            // Đảm bảo bucket tồn tại trước khi upload
            try {
                val bucket = supabase.storage["servicetype"]
                val path = newFileName
                
                bucket.upload(path, inputStream.readBytes())
                
                val publicUrl = bucket.publicUrl(path)
                println("Image uploaded successfully: $publicUrl")
                
                publicUrl
            } catch (e: Exception) {
                if (e.message?.contains("Bucket not found") == true) {
                    println("Bucket 'servicetype' not found, creating it...")
                    // Tạo bucket nếu chưa tồn tại
                    supabase.storage.createBucket(
                        id = "servicetype"
                    )
                    println("Bucket 'servicetype' created, retrying upload...")
                    
                    // Thử upload lại
                    val bucket = supabase.storage["servicetype"]
                    val path = newFileName
                    
                    bucket.upload(path, inputStream.readBytes())
                    
                    val publicUrl = bucket.publicUrl(path)
                    println("Image uploaded successfully after bucket creation: $publicUrl")
                    
                    publicUrl
                } else {
                    throw e
                }
            }
            
        } catch (e: Exception) {
            println("Error uploading image: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    suspend fun updateService(
        serviceId: Long,
        name: String? = null,
        description: String? = null,
        durationMinutes: Int? = null,
        imageUrl: String? = null
    ): Boolean {
        return try {
            println("=== UPDATING SERVICE ===")
            println("ID: $serviceId, Name: $name, Description: $description, Duration: ${durationMinutes}min")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            val updateData = mutableMapOf<String, String>(
                "updated_at" to currentTime
            )
            name?.let { updateData["name"] = it }
            description?.let { updateData["description"] = it }
            durationMinutes?.let { updateData["duration_minutes"] = it.toString() }
            imageUrl?.let { updateData["image_url"] = it }
            
            supabase.from("services")
                .update(updateData) {
                    filter {
                        eq("id", serviceId)
                    }
                }
            
            println("Successfully updated service")
            true
            
        } catch (e: Exception) {
            println("Error updating service: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun deleteService(serviceId: Long): Boolean {
        return try {
            println("=== DELETING SERVICE ===")
            println("Service ID: $serviceId")
            
            supabase.from("services")
                .delete {
                    filter {
                        eq("id", serviceId)
                    }
                }
            
            println("Successfully deleted service")
            true
            
        } catch (e: Exception) {
            println("Error deleting service: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}