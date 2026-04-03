package ups.logic.robbyapp.configuraciones

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
//import androidx.security.crypto.MasterKeys
import androidx.security.crypto.MasterKey

object SecureStorage{

    fun getSecurePrefs(context: Context): SharedPreferences {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Borra el archivo principal
        context.deleteSharedPreferences("secure_prefs")

        // Borra los keysets internos que crea EncryptedSharedPreferences
        context.deleteSharedPreferences("__androidx_security_crypto_encrypted_prefs_key_keyset__")
        context.deleteSharedPreferences("__androidx_security_crypto_encrypted_prefs_value_keyset__")

        // Intenta recrear desde cero
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}


    fun saveSecureString(context: Context, key: String, value: String) {
    try {
        val prefs = getSecurePrefs(context)
        prefs.edit().putString(key, value).apply()
    } catch (e: Exception) {
        context.deleteSharedPreferences("secure_prefs")
        context.deleteSharedPreferences("__androidx_security_crypto_encrypted_prefs_key_keyset__")
        context.deleteSharedPreferences("__androidx_security_crypto_encrypted_prefs_value_keyset__")
    }
}


    @JvmStatic
    fun getSecureString(context: Context, key: String): String? {
    return try {
        val prefs = getSecurePrefs(context)
        prefs.getString(key, null)
    } catch (e: Exception) {
        null
    }
}

}