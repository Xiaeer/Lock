package com.xiaeer.lock.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.drake.statusbar.immersive
import com.xiaeer.lock.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        immersive(darkMode = true)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}