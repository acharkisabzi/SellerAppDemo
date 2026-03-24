package com.example.sellerappdemo.supabase

import android.content.Context
import com.example.sellerappdemo.BuildConfig
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// We use an object to ensure only one instance of the client exists
object Supabase {
    lateinit var client: SupabaseClient

    fun init(context: Context) {
        // This creates a link to Android's storage
        val settings = SharedPreferencesSettings(
            context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
        )

        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Storage)
            install(Auth) {
                // This is the "Magic" line that saves the login session
                sessionManager = SettingsSessionManager(settings)
                alwaysAutoRefresh = true
            }
        }
    }
}

// Shortcut property to keep your existing code working without changes
val supabase: SupabaseClient get() = Supabase.client