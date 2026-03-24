package com.health.calculator.bmi.tracker.data.backup

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

object BackupEncryption {

    private const val KEYSTORE_ALIAS = "health_calc_backup_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        keyStore.getEntry(KEYSTORE_ALIAS, null)?.let { entry ->
            return (entry as KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    fun encrypt(data: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)

        // Prepend IV to encrypted data
        val output = ByteArrayOutputStream()
        output.write(iv)
        output.write(encrypted)
        return output.toByteArray()
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        if (encryptedData.size < IV_SIZE) throw IllegalArgumentException("Invalid encrypted data")
        
        val key = getOrCreateKey()

        val iv = encryptedData.copyOfRange(0, IV_SIZE)
        val cipherText = encryptedData.copyOfRange(IV_SIZE, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(cipherText)
    }

    fun encryptToBase64(data: String): String {
        val encrypted = encrypt(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptFromBase64(base64Data: String): String {
        val encrypted = Base64.decode(base64Data, Base64.NO_WRAP)
        val decrypted = decrypt(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }
}
