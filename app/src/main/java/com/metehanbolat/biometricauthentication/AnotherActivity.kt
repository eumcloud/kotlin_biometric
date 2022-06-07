package com.metehanbolat.biometricauthentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.metehanbolat.biometricauthentication.databinding.ActivitySecondBinding

class AnotherActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}