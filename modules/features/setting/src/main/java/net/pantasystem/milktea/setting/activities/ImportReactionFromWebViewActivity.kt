package net.pantasystem.milktea.setting.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
import kotlinx.coroutines.flow.*
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

        binding.webView.loadUrl("https://$host")


        binding.importButton.setOnClickListener {
            val currentAccount = accountStore.currentAccount
            if (currentAccount != null) {
                val token = getWebClientTokenFromCookie(currentAccount)
                if (token.isNullOrBlank()) {
                    Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_LONG).show()
                } else {
                    importReactionFromWebViewViewModel.onGotWebClientToken(currentAccount, token)
                }
            }
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
                }.filterNotNull().map {
                    val token = getWebClientTokenFromCookie(it)
                    it to token
                }.distinctUntilChanged().collect { (account, token) ->
                    importReactionFromWebViewViewModel.onGotWebClientToken(account, token)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true


        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

        }
    }


    private fun getWebClientTokenFromCookie(account: Account): String? {
        val rawCookie: String? = CookieManager.getInstance().getCookie(account.normalizedInstanceDomain)
        val cookies = rawCookie?.split(";")?.map { it.trim() }?.mapNotNull {
            val key = it.split("=").getOrNull(0)?.lowercase()
            val value = it.split("=").getOrNull(1)
            if (key == null || value == null) {
                null
            } else {
                key to value
            }
        }?.toMap() ?: emptyMap()

        return cookies["token"]
    }


}
