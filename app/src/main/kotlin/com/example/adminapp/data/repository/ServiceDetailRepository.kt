package com.example.adminapp.data.repository

import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.ServiceDetail
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import java.io.InputStream

class ServiceDetailRepository {

    suspend fun getServicesByType(serviceTypeId: Long): List<ServiceDetail> {
        return try {
            println("=== FETCHING SERVICES BY TYPE ID: $serviceTypeId ===")
            
            val result = supabase.from("services")
                .select {
                    filter {
                        eq("service_type_id", serviceTypeId)
                    }
                }
                .decodeList<ServiceDetail>()
                
            println("Successfully fetched ${result.size} services")
            
            // Debug: Print each service
            result.forEachIndexed { index, service ->
                println("Service ${index + 1}: id=${service.id}, name='${service.name}', description='${service.description}', duration=${service.durationMinutes}min")
            }
            
            result
            
        } catch (e: Exception) {
            println("Error fetching services: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun updateServiceStatus(serviceId: Long, isActive: Boolean): Boolean {
        return try {
            println("=== UPDATING SERVICE STATUS ===")
            println("Service ID: $serviceId, Is Active: $isActive")
            
            supabase.from("services")
                .update(
                    mapOf("is_active" to isActive)
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
            
            val insertData = mutableMapOf<String, Any>(
                "name" to name,
                "description" to description,
                "duration_minute" to durationMinutes,
                "service_type_id" to serviceTypeId,
                "is_active" to true
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
            
            val bucket = supabase.storage["service-images"]
            val path = "images/$fileName"
            
            bucket.upload(path, inputStream.readBytes())
            
            val publicUrl = bucket.publicUrl(path)
            println("Image uploaded successfully: $publicUrl")
            
            publicUrl
            
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
            
            val updateData = mutableMapOf<String, Any>()
            name?.let { updateData["name"] = it }
            description?.let { updateData["description"] = it }
            durationMinutes?.let { updateData["duration_minute"] = it }
            imageUrl?.let { updateData["image_url"] = it }
            
            if (updateData.isNotEmpty()) {
                supabase.from("services")
                    .update(updateData) {
                        filter {
                            eq("id", serviceId)
                        }
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