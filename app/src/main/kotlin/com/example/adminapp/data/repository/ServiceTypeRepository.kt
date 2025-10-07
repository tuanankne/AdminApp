package com.example.adminapp.data.repository

import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.ServiceType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.serialization.json.JsonObject
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class ServiceTypeRepository {

    private suspend fun getMaxServiceTypeId(): Long {
        return try {
            // Lấy tất cả service types và tìm max ID
            val serviceTypes = supabase.from("service_types")
                .select()
                .decodeList<ServiceType>()
            
            val maxId = serviceTypes.maxOfOrNull { it.id } ?: 0L
            println("Max ID found: $maxId")
            maxId
        } catch (e: Exception) {
            println("Error getting max ID, using 0: ${e.message}")
            0L
        }
    }

    suspend fun getServiceTypes(): List<ServiceType> {
        return try {
            println("=== FETCHING SERVICE TYPES ===")
            
            // First, try to get raw data to see actual structure
            val rawResult = supabase.from("service_types")
                .select()
                .decodeList<kotlinx.serialization.json.JsonObject>()
            
            println("Raw data structure:")
            rawResult.forEachIndexed { index, obj ->
                println("Row ${index + 1}: $obj")
            }
            
            val result = supabase.from("service_types")
                .select()
                .decodeList<ServiceType>()
                
            println("Successfully fetched ${result.size} service types")
            
            // Debug: Print each service type
            result.forEachIndexed { index, serviceType ->
                println("Service Type ${index + 1}: id=${serviceType.id}, name='${serviceType.name}', isActive=${serviceType.isActive}")
            }
            
            result
            
        } catch (e: Exception) {
            println("Error fetching service types: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun updateServiceTypeStatus(serviceTypeId: Long, isActive: Boolean): Boolean {
        return try {
            println("=== UPDATING SERVICE TYPE STATUS ===")
            println("Service Type ID: $serviceTypeId, Is Active: $isActive")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            supabase.from("service_types")
                .update(
                    mapOf(
                        "is_active" to isActive.toString(),
                        "updated_at" to currentTime
                    )
                ) {
                    filter {
                        eq("id", serviceTypeId)
                    }
                }
            
            println("Successfully updated service type status")
            true
            
        } catch (e: Exception) {
            println("Error updating service type status: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun addServiceType(
        name: String,
        description: String? = null,
        iconUrl: String? = null,
        isActive: Boolean = true
    ): Boolean {
        return try {
            println("=== ADDING NEW SERVICE TYPE ===")
            println("Name: $name, Description: $description, Icon: $iconUrl, Is Active: $isActive")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            // Lấy ID lớn nhất hiện có + 1
            val maxId = getMaxServiceTypeId()
            val id = maxId + 1
            
            val insertData = mutableMapOf<String, String>(
                "id" to id.toString(),                     // ID tự động tạo
                "name" to name,
                "is_active" to isActive.toString(),
                "created_at" to currentTime,    // Thời gian tạo = thời gian hiện tại
                "updated_at" to currentTime      // Thời gian cập nhật = thời gian hiện tại (khi tạo mới)
            )
            
            description?.let { insertData["description"] = it }
            iconUrl?.let { insertData["icon_url"] = it }
            
            supabase.from("service_types")
                .insert(insertData)
            
            println("Successfully added new service type")
            true
            
        } catch (e: Exception) {
            println("Error adding service type: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun uploadImage(inputStream: InputStream, fileName: String): String? {
        return try {
            println("=== UPLOADING IMAGE ===")
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
    
    // Helper function to create sample data for testing
    suspend fun createSampleServiceTypes(): Boolean {
        return try {
            println("=== CREATING SAMPLE SERVICE TYPES ===")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            // Lấy ID lớn nhất hiện có
            val maxId = getMaxServiceTypeId()
            
            val sampleTypes = listOf(
                mapOf(
                    "id" to (maxId + 1).toString(),  // ID tự động tạo
                    "name" to "Dọn dẹp nhà cửa",
                    "icon_url" to null,
                    "is_active" to "true",
                    "created_at" to currentTime,    // Thời gian tạo = thời gian hiện tại
                    "updated_at" to currentTime     // Thời gian cập nhật = thời gian hiện tại (khi tạo mới)
                ),
                mapOf(
                    "id" to (maxId + 2).toString(),  // ID tự động tạo
                    "name" to "Sửa chữa điện nước",
                    "icon_url" to null,
                    "is_active" to "true",
                    "created_at" to currentTime,
                    "updated_at" to currentTime
                ),
                mapOf(
                    "id" to (maxId + 3).toString(),  // ID tự động tạo
                    "name" to "Vệ sinh máy lạnh",
                    "icon_url" to null,
                    "is_active" to "true",
                    "created_at" to currentTime,
                    "updated_at" to currentTime
                )
            )
            
            sampleTypes.forEach { serviceType ->
                supabase.from("service_types")
                    .insert(serviceType)
                println("Inserted: ${serviceType["name"]}")
            }
            
            println("Sample service types created successfully")
            true
            
        } catch (e: Exception) {
            println("Error creating sample service types: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun updateServiceType(
        serviceTypeId: Long,
        name: String,
        description: String? = null,
        iconUrl: String? = null
    ): Boolean {
        return try {
            println("=== UPDATING SERVICE TYPE ===")
            println("ID: $serviceTypeId, Name: $name, Description: $description, Icon: $iconUrl")
            
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            val updateData = mutableMapOf<String, String>(
                "name" to name,
                "updated_at" to currentTime
            )
            
            // Add description if not null
            description?.let { updateData["description"] = it }
            
            // Only update icon_url if provided (new image uploaded)
            iconUrl?.let { updateData["icon_url"] = it }
            
            supabase.from("service_types")
                .update(updateData) {
                    filter {
                        eq("id", serviceTypeId)
                    }
                }
            
            println("Successfully updated service type")
            true
            
        } catch (e: Exception) {
            println("Error updating service type: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun deleteServiceType(serviceTypeId: Long): Boolean {
        return try {
            println("=== DELETING SERVICE TYPE ===")
            println("Service Type ID: $serviceTypeId")
            
            supabase.from("service_types")
                .delete {
                    filter {
                        eq("id", serviceTypeId)
                    }
                }
            
            println("Successfully deleted service type")
            true
            
        } catch (e: Exception) {
            println("Error deleting service type: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}