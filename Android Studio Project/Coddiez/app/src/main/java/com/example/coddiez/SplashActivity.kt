package com.example.coddiez

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState)

        startActivity(Intent(this, MainActivity::class.java))

        // close splash activity
        finish()

    }
}