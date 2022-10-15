package net.pantasystem.milktea.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_AUTH_URL = "EXTRA_AUTH_URL"

class WebViewAuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_auth)

        val authUrl = intent.getStringExtra(EXTRA_AUTH_URL)
    }
}