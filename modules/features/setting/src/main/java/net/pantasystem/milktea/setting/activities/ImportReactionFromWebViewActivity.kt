package net.pantasystem.milktea.setting.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.reaction.ReactionChoicesAdapter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.databinding.ActivityImportReactionFromWebViewBinding
import net.pantasystem.milktea.setting.viewmodel.ImportReactionFromWebViewViewModel
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
        "milktea"
    }

    private val binding: ActivityImportReactionFromWebViewBinding by dataBinding()

    private val importReactionFromWebViewViewModel: ImportReactionFromWebViewViewModel by viewModels()

    @Inject
    lateinit var applyTheme: ApplyTheme

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_import_reaction_from_web_view)

        setupWebView()
        setSupportActionBar(binding.topToolbar)
        supportActionBar?.setTitle(R.string.import_reactions_from_the_web)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    android.R.id.home -> {
                        finish()
                    }
                }
                return true
            }
        })

        binding.webView.loadUrl("https://$host/settings/reaction")


        binding.importButton.setOnClickListener {
            executeImportReactionsScript(accountStore.currentAccount)
        }

        val adapter = ReactionChoicesAdapter {}

        binding.reactionsAdapter.adapter = adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                importReactionFromWebViewViewModel.reactions.collect {
                    adapter.submitList(it)
                }
            }
        }
        binding.reactionsAdapter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.overrideSaveButton.setOnClickListener {
            importReactionFromWebViewViewModel.onOverwriteButtonClicked()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                (0..Int.MAX_VALUE).asFlow().map {
                    delay(500)
                }.flatMapLatest {
                    accountStore.observeCurrentAccount
                }.collect {
                    executeImportReactionsScript(it)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.addJavascriptInterface(
            jsInterface, jsInterfaceName
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
        val script = """
        $jsInterfaceName.setLocalStorageCache(
            window.localStorage
                .getItem(
                    'pizzax::base::cache::${current?.remoteId}'
                )
            )
        """.trimIndent()
        Log.d("ImportReactionFrom", "script:$script")
        binding.webView.evaluateJavascript(
            script
        ) {
            Log.d("ImportReactionFrom", "result:$it")
        }
    }

    private val jsInterface: LocalStorageResultJSInterface by lazy {
        LocalStorageResultJSInterface(importReactionFromWebViewViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.removeJavascriptInterface(jsInterfaceName)
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