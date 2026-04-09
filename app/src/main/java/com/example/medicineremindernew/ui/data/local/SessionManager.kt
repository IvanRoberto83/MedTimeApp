package com.example.medicineremindernew.ui.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveEmail(email: String) {
        prefs.edit().putString("USER_EMAIL", email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString("USER_EMAIL", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
