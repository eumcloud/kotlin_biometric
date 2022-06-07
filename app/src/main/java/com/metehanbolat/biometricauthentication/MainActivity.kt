package com.metehanbolat.biometricauthentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.metehanbolat.biometricauthentication.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val TAG = "MainActivity"
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /** In this section, you can take action according to the status and information of the biometric hardware of the device. */
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Log.d(TAG, "BIOMETRIC_SUCCESS")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Log.e(TAG, "BIOMETRIC_ERROR_HW_UNAVAILABLE")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Log.e(TAG, "BIOMETRIC_ERROR_NO_HARDWARE")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Log.e(TAG, "BIOMETRIC_ERROR_NONE_ENROLLED")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Log.e(TAG, "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Log.e(TAG, "BIOMETRIC_ERROR_UNSUPPORTED")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> Log.e(TAG, "BIOMETRIC_STATUS_UNKNOWN")
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("You can enter using your biometric credentials")
            .setNegativeButtonText("Cancel")
            .build()

        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, errString.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                    Intent(this@MainActivity, AnotherActivity::class.java).apply {
                        startActivity(this)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
            })

        binding.button.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

    }
}