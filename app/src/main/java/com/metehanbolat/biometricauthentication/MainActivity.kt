package com.metehanbolat.biometricauthentication

import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.metehanbolat.biometricauthentication.databinding.ActivityMainBinding
import java.io.File
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private val ImageView.surfaceProvider: Preview.SurfaceProvider?
    get() = null

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val TAG = "MainActivity"
//    BIOMETRIC val
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

//    CAMERA val
    private val imageCapture:ImageCapture?=null
    private lateinit var outputDirectory:File
    private lateinit var cameraExecutor:ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        camera
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

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


        if(allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }


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
            takePhoto()
        }

    }
    private fun getOutputDirectory():File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let{
            mFile->
                File(mFile, resources.getString(R.string.app_name)).apply{
                    mkdirs()
                }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photofile = File(
            outputDirectory,
            SimpleDateFormat(Constants.FILE_NAME_FORMAT,
                Locale.getDefault())
                    .format(System.currentTimeMillis())+".jpg")
        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photofile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photofile)
                    val msg = "Photo Saved"

                    Toast.makeText(this@MainActivity, "$msg $savedUri", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "onError:${exception.message}", exception)
                }

            }
        )

    }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all{
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array< String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS){
            if(allPermissionGranted()){
                startCamera()
            }else{
                Toast.makeText(this,
                "Permissions not granted",
                Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroy(){
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance( this )
        cameraProviderFuture.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
              .build()
              .also{ mPreview->
                  mPreview.setSurfaceProvider(
                      binding.viewFinder.surfaceProvider
                  )
            }

//        imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }catch(e: Exception){
                Log.d(TAG, "start Camera Failed", e)
            }
        }, ContextCompat.getMainExecutor(this))

    }

}