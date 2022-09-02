package com.huawei.hms.knowmyboard.dtse.activity.view

import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.knowmyboard.dtse.R
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val decorView = window.decorView
        val options = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = options
        setTheme(R.style.FullScreen)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val SPLASH_DURATION = 3000
        Handler().postDelayed({
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DURATION.toLong())
    }
}