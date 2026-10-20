package com.otofarma.telehealth.data

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    @Volatile
    var baseUrl: String = BASE_URL
        private set

    @Volatile
    var token: String? = prefs.getString(KEY_TOKEN, null)
        private set

    @Volatile
    var name: String = prefs.getString(KEY_NAME, "") ?: ""
        private set

    @Volatile
    var email: String = prefs.getString(KEY_EMAIL, "") ?: ""
        private set

    fun saveSession(token: String, name: String, email: String) {
        this.token = token
        this.name = name
        this.email = email
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun clearSession() {
        token = null
        name = ""
        prefs.edit().remove(KEY_TOKEN).remove(KEY_NAME).apply()
    }

    companion object {
        const val BASE_URL = "http://159.69.22.142:8000"
        private const val KEY_TOKEN = "token"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
    }
}
