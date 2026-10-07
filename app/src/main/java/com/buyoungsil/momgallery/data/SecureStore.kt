package com.buyoungsil.momgallery.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// 아이디·비밀번호·로그인 쿠키를 암호화해서 폰에 저장해요.
class SecureStore(context: Context) {

    private val prefs: SharedPreferences = open(context.applicationContext)

    val username: String? get() = prefs.getString(KEY_USER, null)
    val password: String? get() = prefs.getString(KEY_PASS, null)

    fun hasCredentials(): Boolean = !username.isNullOrEmpty() && !password.isNullOrEmpty()

    fun saveCredentials(user: String, pass: String) {
        prefs.edit().putString(KEY_USER, user).putString(KEY_PASS, pass).apply()
    }

    fun clearCredentials() {
        prefs.edit().remove(KEY_USER).remove(KEY_PASS).apply()
    }

    fun loadCookies(): List<String> =
        prefs.getStringSet(KEY_COOKIES, emptySet())?.toList().orEmpty()

    fun saveCookies(cookies: List<String>) {
        prefs.edit().putStringSet(KEY_COOKIES, cookies.toSet()).apply()
    }

    private companion object {
        const val FILE = "secure_prefs"
        const val KEY_USER = "username"
        const val KEY_PASS = "password"
        const val KEY_COOKIES = "cookies"

        fun open(context: Context): SharedPreferences {
            fun create(): SharedPreferences {
                val key = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                return EncryptedSharedPreferences.create(
                    context,
                    FILE,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            }
            // 폰의 보안 키가 꼬인 드문 경우: 저장소를 비우고 다시 만들어요 (다시 로그인하면 돼요)
            return try {
                create()
            } catch (e: Exception) {
                context.deleteSharedPreferences(FILE)
                create()
            }
        }
    }
}