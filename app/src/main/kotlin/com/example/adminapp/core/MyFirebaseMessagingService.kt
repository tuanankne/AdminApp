package com.example.adminapp.core

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import androidx.core.content.edit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Use SupervisorJob to prevent job cancellation
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token received: $token")
        
        // Gửi token này lên Supabase
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")
        
        if (userId.isNullOrEmpty()) {
            Log.w("FCM", "No user ID found, will save token when user logs in")
            // Save token locally for later use
            sharedPreferences.edit() { putString("pending_fcm_token", token) }
            return
        }

        serviceScope.launch {
            try {
                SupabaseTokenUploader.sendTokenToSupabase(token, userId)
                Log.d("FCM", "Token successfully sent to Supabase for user: $userId")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token to Supabase: ${e.message}")
                // Save token locally for retry
                sharedPreferences.edit() { putString("pending_fcm_token", token) }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        // Hiển thị Notification nếu muốn
        val title = remoteMessage.notification?.title ?: "Notification"
        val body = remoteMessage.notification?.body ?: ""
        val type = remoteMessage.data["type"] ?: "general"
        
        // Lưu notification vào database
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")
        
        if (!userId.isNullOrEmpty()) {
            saveNotificationToDatabase(userId, title, body, type, remoteMessage.data)
        }

        showNotification(title, body)
    }

    @SuppressLint("ServiceCast")
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo kênh thông báo với mức độ quan trọng cao để đảm bảo hiển thị
        val channelId = "fcm_default_channel"
        val channelName = "Thông báo đẩy"
        val channel = NotificationChannel(
            channelId, 
            channelName, 
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo từ ứng dụng"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)

        // Tạo intent để mở ứng dụng khi nhấn thông báo
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Thêm extra để điều hướng đến NotificationScreen
            putExtra("navigate_to", "notifications")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Thay bằng icon ứng dụng
            .setContentIntent(pendingIntent) // Nhấn để mở ứng dụng
            .setAutoCancel(true) // Tự động đóng khi nhấn
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Mức độ ưu tiên cao
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Âm thanh, rung, đèn
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // Hiển thị văn bản đầy đủ
            .build()

        // Sử dụng timestamp làm ID thông báo để tránh ghi đè
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        
        Log.d("FCM", "System notification displayed with ID: $notificationId")
    }

    private fun saveNotificationToDatabase(
        userId: String, 
        title: String, 
        body: String, 
        type: String, 
        data: Map<String, String>
    ) {
        serviceScope.launch {
            try {
                Log.d("FCM", "🔄 Starting to save notification to database")
                val jsonData = JsonObject(
                    data.mapValues { JsonPrimitive(it.value) }
                )

                val notification = NotificationInsert(
                    userId = userId,
                    title = title,
                    body = body,
                    type = type,
                    data = jsonData
                )

                supabase.postgrest
                    .from("notifications")
                    .insert(notification)
                
                Log.d("FCM", "✅ Notification saved to database: $title")
                
                // Gửi broadcast để thông báo cho UI
                val broadcastIntent = Intent(NEW_NOTIFICATION_ACTION).apply {
                    putExtra(EXTRA_NOTIFICATION_TYPE, type)
                    addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                    `package` = applicationContext.packageName
                }
                
                Log.d("FCM", "📢 Preparing to send broadcasts")
                Log.d("FCM", "📢 Broadcast details:")
                Log.d("FCM", "   - Action: ${broadcastIntent.action}")
                Log.d("FCM", "   - Type: $type")
                Log.d("FCM", "   - Package: ${broadcastIntent.`package`}")
                Log.d("FCM", "   - Flags: ${broadcastIntent.flags}")
                
                try {
                    // Send global broadcast
                    applicationContext.sendBroadcast(broadcastIntent)
                    Log.d("FCM", "✅ Successfully sent global broadcast")
                } catch (e: Exception) {
                    Log.e("FCM", "❌ Failed to send broadcast: ${e.message}", e)
                    Log.e("FCM", "Stack trace: ", e)
                }
            } catch (e: Exception) {
                Log.e("FCM", "❌ Error saving notification to database: ${e.message}")
                Log.e("FCM", "Stack trace: ", e)
            }
        }
    }

    companion object {
        // Broadcast constants
        const val NEW_NOTIFICATION_ACTION = "com.example.adminapp.NEW_NOTIFICATION"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        /**
         * Generate and upload FCM token for current user
         */
        fun generateAndUploadToken(context: Context, userId: String) {
            Log.d("FCM", "Generating FCM token for user: $userId")
            
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d("FCM", "FCM registration token: $token")

                // Send token to Supabase
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        SupabaseTokenUploader.sendTokenToSupabase(token, userId)
                        Log.d("FCM", "Token uploaded successfully for user: $userId")
                        
                        // Clear pending token
                        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            .edit() { remove("pending_fcm_token") }
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to upload token: ${e.message}")
                    }
                }
            }
        }
        
        /**
         * Upload pending FCM token if exists
         */
        fun uploadPendingToken(context: Context, userId: String) {
            val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val pendingToken = sharedPreferences.getString("pending_fcm_token", null)
            
            if (!pendingToken.isNullOrEmpty()) {
                Log.d("FCM", "Uploading pending FCM token for user: $userId")
                
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        SupabaseTokenUploader.sendTokenToSupabase(pendingToken, userId)
                        Log.d("FCM", "Pending token uploaded successfully")
                        
                        // Clear pending token
                        sharedPreferences.edit() { remove("pending_fcm_token") }
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to upload pending token: ${e.message}")
                    }
                }
            }
        }
    }
}
@Serializable
data class UserPushToken(
    @SerialName("user_id")
    val userId: String,
    val token: String
)

object SupabaseTokenUploader {

    suspend fun sendTokenToSupabase(token: String, userId: String) {
        try {
            Log.d("FCM", "Uploading token for user $userId: ${token.take(20)}...")
            
            // First, try to update existing token
            val updateResult = withContext(Dispatchers.IO) {
                supabase.postgrest
                    .from("user_push_tokens")
                    .update(
                        mapOf("token" to token, "updated_at" to "now()")
                    ) {
                        select()
                        filter {
                            eq("user_id", userId)
                        }
                    }
            }.decodeList<UserPushToken>()
            
            Log.d("FCM", "Token update result: $updateResult")
            
            // If no rows were updated, insert new token
            if (updateResult.isEmpty()) {
                Log.d("FCM", "No existing token found, inserting new one")
                val insertResult = withContext(Dispatchers.IO) {
                    supabase.postgrest
                        .from("user_push_tokens")
                        .insert(
                            mapOf(
                                "user_id" to userId,
                                "token" to token
                            )
                        ){
                            select()
                        }.decodeList<UserPushToken>()
                }
                Log.d("FCM", "Token inserted: $insertResult")
            } else {
                Log.d("FCM", "Token updated successfully")
            }
            
        } catch (e: Exception) {
            Log.e("FCM", "Error saving token: ${e.message}")
            Log.e("FCM", "Error details: ", e)
            throw e
        }
    }
}
@Serializable
data class NotificationInsert(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val data: JsonObject
)

