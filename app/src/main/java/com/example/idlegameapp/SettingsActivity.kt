package com.example.idlegameapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<MaterialButton>(R.id.websiteButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fast.com"))
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
} 