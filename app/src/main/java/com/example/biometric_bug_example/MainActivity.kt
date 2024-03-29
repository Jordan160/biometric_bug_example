package com.example.biometric_bug_example

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricPromptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        ButterKnife.bind(this)
        setContentView(R.layout.activity_main)

        // Set up prompt
        biometricPrompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(baseContext),
                object : BiometricPrompt.AuthenticationCallback() {
                    // no-op
                }
        )

        // Prompt info
        biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Title")
                .setNegativeButtonText("Test")
                .build()

        // Generates key
        findViewById<Button>(R.id.count_btn).setOnClickListener {
            generateKey()
        }

        // Shows biometric prompt
        findViewById<Button>(R.id.random_btn).setOnClickListener {
            getCrypto()?.let { it1 -> biometricPrompt.authenticate(biometricPromptInfo, it1) }
                    ?: run {
                        // Key is invalidated - this is never triggered
                    }
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator
                .getInstance(
                    KeyProperties
                        .KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(getKeyGen())
        keyGenerator.generateKey()
    }

    private fun getCipher() : Cipher {
        return Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    private fun getKey() : SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey("key6", null) as SecretKey
    }


    private fun getKeyGen() : KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
                "key6",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
                .build()
    }

    private fun getCrypto() : BiometricPrompt.CryptoObject? {
        return try {
            val cipher = getCipher()
            val key = getKey()
            cipher.init(Cipher.ENCRYPT_MODE, key)
            BiometricPrompt.CryptoObject(cipher)
        } catch(e: KeyPermanentlyInvalidatedException) {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry("key6")
            null
        }
    }
}