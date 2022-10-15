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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wada811.databinding.dataBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.auth.databinding.ActivityWebViewAuthBinding

const val EXTRA_AUTH_URL = "EXTRA_AUTH_URL"

class WebViewAuthActivity : AppCompatActivity() {
    val binding by dataBinding<ActivityWebViewAuthBinding>()

    private val authUrl by lazy {
        intent.getStringExtra(EXTRA_AUTH_URL)
    }

    private val username by lazy {
        intent.getStringExtra(EXTRA_USERNAME)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_auth)

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

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                delayedLoad()
            }
        }

        binding.webView.loadUrl(authUrl!!)


    }

    fun delayedLoad() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                (0 until Int.MAX_VALUE).asFlow().map {
                    delay(500)
                }.filter {
                    binding.webView.url == authUrl
                }.filterNot {
                    username.isNullOrBlank()
                }.map {
                    load()
                }.collect {

                }
            }
        }
    }
    private fun load() {
        binding.webView.loadUrl("""
            javascript:console.log(
                (function(){
                    const tag = Array.from(document.getElementsByTagName('input'))
                        .filter(e => e.type=='text')[0];
                    if ('$username' && tag.value != '$username' && !tag.value) {
                        tag.value = '$username';
                    }
                }())
            )
        """.trimIndent())
    }

}

//Array.from(document.getElementsByTagName('input'))
//.filter(e => e.type=='text')[0].value = '$username'