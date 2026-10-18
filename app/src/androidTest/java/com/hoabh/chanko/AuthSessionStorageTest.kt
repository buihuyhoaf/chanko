package com.hoabh.chanko

import androidx.test.platform.app.InstrumentationRegistry
import com.hoabh.chanko.data.auth.KeystoreAuthSessionStore
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AuthSessionStorageTest {
    @Test fun sessionIsEncryptedAndDurableOutsideBackup() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = KeystoreAuthSessionStore(context)
        store.save("instrumented-access-proof", "instrumented-refresh-proof")
        val file = File(context.noBackupFilesDir, "auth-session.enc")
        val bytes = file.readBytes()
        assertFalse(bytes.toString(Charsets.ISO_8859_1).contains("instrumented-access-proof"))
        assertFalse(bytes.toString(Charsets.ISO_8859_1).contains("instrumented-refresh-proof"))
        assertEquals(1, bytes[0].toInt())
        val ivSize = bytes[1].toInt()
        val key = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            .getKey("chanko.auth.session.v1", null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(2, 2 + ivSize)))
        val plain = JSONObject(cipher.doFinal(bytes.copyOfRange(2 + ivSize, bytes.size)).toString(Charsets.UTF_8))
        assertEquals("instrumented-access-proof", plain.getString("access_token"))
        assertEquals("instrumented-refresh-proof", plain.getString("refresh_token"))
        store.save("rotated-access", "rotated-refresh")
        assertFalse(bytes.contentEquals(file.readBytes()))
        file.delete()
        Unit
    }
}
