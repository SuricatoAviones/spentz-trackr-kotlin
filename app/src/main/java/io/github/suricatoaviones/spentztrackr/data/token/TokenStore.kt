package io.github.suricatoaviones.spentztrackr.data.token

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token Sanctum en almacenamiento cifrado (ADR-A10): clave maestra en el
 * Android Keystore. El backend lo expira a los 90 días; ante 401 la app pide
 * re-login, no guardamos la contraseña.
 */
@Singleton
class TokenStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val prefs: SharedPreferences by lazy {
            val masterKey =
                MasterKey
                    .Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        fun token(): String? = prefs.getString(KEY_TOKEN, null)

        fun save(token: String) {
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }

        fun clear() {
            prefs.edit().remove(KEY_TOKEN).apply()
        }

        private companion object {
            const val FILE_NAME = "spentz_secure_prefs"
            const val KEY_TOKEN = "sanctum_token"
        }
    }
