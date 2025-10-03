package com.example.adminapp.core

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import kotlin.time.Duration.Companion.seconds

//object SupabaseClient {
//    val client: SupabaseClient = createSupabaseClient(
//        supabaseUrl = "https://uyxudwhglwwbvbnjgwmw.supabase.co", // Thay bằng URL của bạn
//        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV5eHVkd2hnbHd3YnZibmpnd213Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NjYwMzcyMywiZXhwIjoyMDYyMTc5NzIzfQ.gc0tdCTkAeGXxvM9VSM1iv6uxEqcQELbmWkX-Ms0Q_M"      // Thay bằng Anon Key của bạn
//    ) {
//        install(Postgrest)
//        install(Realtime)
//        install(Auth)
//    }
//
//    init {
//        Log.d("Supabase", "Supabase client initialized")
//    }
//}

@OptIn(SupabaseInternal::class)
val supabase = createSupabaseClient (
    supabaseUrl = "https://uyxudwhglwwbvbnjgwmw.supabase.co", // Thay bằng URL của bạn
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV5eHVkd2hnbHd3YnZibmpnd213Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY2MDM3MjMsImV4cCI6MjA2MjE3OTcyM30.7YESzMNtqbpyo9nJ7bxw3c4X556fv1Erxg9PvONoGtg",      // Thay bằng Anon Key của bạn

    ){
    // 1. Truyền engine tại đây
    httpEngine = CIO.create()

    // 2. Cấu hình thêm nếu muốn
    httpConfig {
        install(WebSockets)
        // Optional: thêm timeout, logging...
    }

    install(Postgrest)
    install(Realtime){
        reconnectDelay = 5.seconds
    }
    install(Auth)
    install(Storage)
}
