package net.pantasystem.milktea.setting.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.databinding.ActivityImportReactionFromWebViewBinding
import net.pantasystem.milktea.setting.viewmodel.ImportReactionFromWebViewViewModel
import java.util.*
import javax.inject.Inject

const val EXTRA_ACCOUNT_HOST = "EXTRA_ACCOUNT_HOST"

@AndroidEntryPoint
class ImportReactionFromWebViewActivity : AppCompatActivity() {

    @Inject
    lateinit var accountStore: AccountStore

    private val host by lazy {
        intent.getStringExtra(EXTRA_ACCOUNT_HOST)
    }

    private val jsInterfaceName by lazy {
        UUID.randomUUID().toString()
    }

    private val binding: ActivityImportReactionFromWebViewBinding by dataBinding()

    private val importReactionFromWebViewViewModel: ImportReactionFromWebViewViewModel by viewModels()


    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_reaction_from_web_view)

        setupWebView()

        binding.webView.loadUrl("https://$host/settings/reaction")


        binding.importButton.setOnClickListener {
            executeImportReactionsScript(accountStore.currentAccount)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.addJavascriptInterface(
            LocalStorageResultJSInterface(
                importReactionFromWebViewViewModel
            ), jsInterfaceName
        )

        val current = accountStore.currentAccount

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                executeImportReactionsScript(current)
            }

        }
    }

    fun executeImportReactionsScript(current: Account?) {
        binding.webView.evaluateJavascript(
            """
                $jsInterfaceName.setLocalStorageCache(
                    window.localStorage
                        .getItem(
                            'pizzax::base::cache::${current?.remoteId}'
                        )
                )
                """.trimIndent()
        ) {
            Log.d("ImportReactionFrom", "result:$it")
        }
    }
}

class LocalStorageResultJSInterface(
    private val importReactionFromWebViewViewModel: ImportReactionFromWebViewViewModel
) {

    @JavascriptInterface
    fun setLocalStorageCache(cache: String) {
        Log.d("setLocalStorageCache", "cache:$cache")
        importReactionFromWebViewViewModel.onLocalStorageCacheLoaded(cache)
    }
}