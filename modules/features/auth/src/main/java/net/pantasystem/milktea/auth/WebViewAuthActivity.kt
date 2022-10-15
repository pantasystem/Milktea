package net.pantasystem.milktea.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.wada811.databinding.dataBinding
import net.pantasystem.milktea.auth.databinding.ActivityWebViewAuthBinding

const val EXTRA_AUTH_URL = "EXTRA_AUTH_URL"

class WebViewAuthActivity : AppCompatActivity() {
    val binding by dataBinding<ActivityWebViewAuthBinding>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_auth)

        val authUrl = intent.getStringExtra(EXTRA_AUTH_URL)
        WebStorage.getInstance().deleteAllData()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url
                Log.d("WebViewAuthActivity", "shouldOverrideUrlLoading uri:$url")

                if (url != null) {
                    if (url.scheme == "misskey" && url.host == "app_auth_callback") {
                        Log.d("WebViewAuthActivity", "該当する")
                        startActivity(Intent(Intent.ACTION_VIEW, url))
                        return true
                    }
                }
                return false
            }
        }

        binding.webView.loadUrl(authUrl!!)
    }

}