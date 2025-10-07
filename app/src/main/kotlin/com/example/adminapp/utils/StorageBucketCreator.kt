package com.example.adminapp.utils

import com.example.adminapp.core.supabase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StorageBucketCreator {
    
    suspend fun createRequiredBuckets(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("=== CREATING STORAGE BUCKETS ===")
                
                // Danh sách các bucket cần tạo
                val buckets = listOf(
                    BucketConfig(
                        id = "servicetype",
                        name = "servicetype",
                        public = true,
                        fileSizeLimit = 52428800L, // 50MB
                        allowedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/gif")
                    ),
                    BucketConfig(
                        id = "report-images",
                        name = "report-images", 
                        public = true,
                        fileSizeLimit = 52428800L, // 50MB
                        allowedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/gif")
                    ),
                    BucketConfig(
                        id = "user-avatars",
                        name = "user-avatars",
                        public = true,
                        fileSizeLimit = 10485760L, // 10MB
                        allowedMimeTypes = listOf("image/jpeg", "image/png", "image/webp", "image/gif")
                    )
                )
                
                // Tạo từng bucket
                buckets.forEach { bucketConfig ->
                    try {
                        println("Creating bucket: ${bucketConfig.id}")
                        supabase.storage.createBucket(
                            id = bucketConfig.id
                        )
                        println("✅ Bucket '${bucketConfig.id}' created successfully")
                    } catch (e: Exception) {
                        if (e.message?.contains("already exists") == true) {
                            println("ℹ️ Bucket '${bucketConfig.id}' already exists")
                        } else {
                            println("❌ Error creating bucket '${bucketConfig.id}': ${e.message}")
                        }
                    }
                }
                
                println("=== BUCKET CREATION COMPLETED ===")
                true
                
            } catch (e: Exception) {
                println("❌ Error creating buckets: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
    
    data class BucketConfig(
        val id: String,
        val name: String,
        val public: Boolean,
        val fileSizeLimit: Long,
        val allowedMimeTypes: List<String>
    )
}
