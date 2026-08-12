package fr.quickvpn.android.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "quickvpn_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_TOKEN).apply()
            else prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    var userEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_EMAIL).apply()
            else prefs.edit().putString(KEY_EMAIL, value).apply()
        }

    var wgConfig: String?
        get() = prefs.getString(KEY_CONFIG, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_CONFIG).apply()
            else prefs.edit().putString(KEY_CONFIG, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_TOKEN = "api_token"
        const val KEY_EMAIL = "user_email"
        const val KEY_CONFIG = "wg_config"
    }
}
