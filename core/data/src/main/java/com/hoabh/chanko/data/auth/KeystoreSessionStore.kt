package com.hoabh.chanko.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import java.io.File
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** One encrypted active session; business cache/drafts remain in their own account partitions. */
class KeystoreSessionStore(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SecureSessionStore {
    private val legacy = AtomicFile(File(context.noBackupFilesDir, "auth-session.enc"))
    private val file = AtomicFile(File(context.noBackupFilesDir, "auth-foundation-session-v1.enc"))
    override suspend fun read(): SessionCredentials? = withContext(ioDispatcher) {
        if (!exists()) return@withContext null
        val bytes = file.readFully()
        if (bytes.size < 30 || bytes.size > 65536 || bytes[0].toInt() != 1)
            throw IOException("Invalid session format")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = existingKey() ?: throw IOException("Session key unavailable")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(1, 13)))
        val plain = cipher.doFinal(bytes, 13, bytes.size - 13)
        try {
            val value = JSONObject(String(plain, Charsets.UTF_8))
            SessionCredentials(AccountScope(value.getString("account")), value.getString("access"),
                value.getString("refresh"), value.getLong("issued"), value.getLong("expires"),
                value.getLong("created"), value.getLong("refreshed"))
        } catch (_: org.json.JSONException) { throw IOException("Invalid session content")
        } catch (_: IllegalArgumentException) { throw IOException("Invalid session account")
        } finally { plain.fill(0) }
    }
    override suspend fun replace(credentials: SessionCredentials) = withContext(ioDispatcher) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, existingKey() ?: createKey())
        val plain = JSONObject().put("account", credentials.account.value).put("access", credentials.access)
            .put("refresh", credentials.refresh).put("issued", credentials.issuedAt)
            .put("expires", credentials.accessExpiresAt).put("created", credentials.createdAt)
            .put("refreshed", credentials.refreshedAt).toString().toByteArray(Charsets.UTF_8)
        val encrypted = try { cipher.doFinal(plain) } finally { plain.fill(0) }
        if (cipher.iv.size != 12 || encrypted.size + 13 > 65536) throw IOException("Invalid session size")
        val output = file.startWrite()
        try {
            output.write(1); output.write(cipher.iv); output.write(encrypted)
            file.finishWrite(output)
        } catch (e: Exception) { file.failWrite(output); throw e }
    }
    override suspend fun clear() = withContext(ioDispatcher) {
        file.delete()
        legacy.delete() // Retire the unscoped legacy credentials; never infer their account.
        if (exists() || legacy.baseFile.exists() || File(legacy.baseFile.path + ".bak").exists()) throw IOException("Session removal failed")
    }
    private fun exists() = file.baseFile.exists() || File(file.baseFile.path + ".bak").exists()
    private fun existingKey(): SecretKey? = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        .getKey(ALIAS, null) as? SecretKey
    private fun createKey(): SecretKey = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
        init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
    }.generateKey()
    private companion object { const val ALIAS = "chanko.auth.foundation.v1" }
}
