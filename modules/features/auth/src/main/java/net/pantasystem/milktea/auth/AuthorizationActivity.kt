package net.pantasystem.milktea.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.whenResumed
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.auth.viewmodel.app.AppAuthViewModel
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.data.infrastructure.auth.from
import java.util.*
import javax.inject.Inject


const val EXTRA_HOST = "EXTRA_HOST"
const val EXTRA_USERNAME = "EXTRA_USERNAME"

class AuthorizationNavigationImpl @Inject constructor(
    val activity: Activity
) : AuthorizationNavigation {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun newIntent(args: AuthorizationArgs): Intent {
        val intent = Intent(activity, AuthorizationActivity::class.java)
        when (args) {
            is AuthorizationArgs.New -> {}
            is AuthorizationArgs.ReAuth -> {
                intent.putExtra(EXTRA_HOST, args.account?.getHost())
                intent.putExtra(EXTRA_USERNAME, args.account?.userName)
            }
        }
        return intent
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthorizationActivity : AppCompatActivity() {

    private val appAuthViewModel: AppAuthViewModel by viewModels()

    @Inject
    lateinit var mainNavigation: MainNavigation

    @Inject
    lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
//        setContentView(R.layout.activity_authorization)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                appAuthViewModel.state.collect {
                    Log.d("AuthorizationActivity", "state:$it")
                    if (it.stateType is Authorization.Finish) {
                        startActivity(mainNavigation.newIntent(Unit))
                        finish()
                        return@collect
                    }
                }
            }
        }

        lifecycleScope.launch {
            whenResumed {
                appAuthViewModel.waiting4UserAuthorizationStepEvent.collect {
                    if (appAuthViewModel.isOpenInWebView.value) {
                        startActivity(
                            Intent(
                                this@AuthorizationActivity,
                                WebViewAuthActivity::class.java
                            ).also { intent ->
                                intent.putExtra(EXTRA_AUTH_URL, it.generateAuthUrl())
                                intent.putExtra(EXTRA_USERNAME, appAuthViewModel.username.value)
                            })
                    } else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.generateAuthUrl())))
                    }
                }
            }
        }


        setContent {
            MdcTheme {
                AuthScreen(authViewModel = appAuthViewModel,
                    onCopyToClipboard = {
                        (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also { clipboardManager ->
                            it.let {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "misskey auth url",
                                        it
                                    )
                                )
                                Toast.makeText(
                                    this,
                                    getString(R.string.copied_to_clipboard),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onShowPrivacyPolicy = {
                        showPrivacyPolicy()
                    },
                    onShowTermsOfService = {
                        showTermsOfService()
                    },
                    onSignUpButtonClicked = {
                        val intent = Intent(this, SignUpActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }

        val username = intent.getStringExtra(EXTRA_USERNAME)
        val host = intent.getStringExtra(EXTRA_HOST)
        if (host != null) {
            appAuthViewModel.instanceDomain.value = host
        }
        if (username != null) {
            appAuthViewModel.username.value = username
        }

    }


    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val authStore = CustomAuthStore.newInstance(this)
        val callbackToken = intent?.data?.getQueryParameter("token")
        val callbackMastodonCode = intent?.data?.getQueryParameter("code")

        if (callbackToken?.isNotBlank() == true) {
            authStore.getCustomAuthBridge()?.let {
                val state = Authorization.Waiting4UserAuthorization.from(it)
                appAuthViewModel.getAccessToken(w4a = state)

            }
        } else if (callbackMastodonCode?.isNotBlank() == true) {
            authStore.getCustomAuthBridge()?.let {
                val state = Authorization.Waiting4UserAuthorization.from(it)
                appAuthViewModel.getAccessToken(callbackMastodonCode, w4a = state)
            }
        }
    }

    private fun showPrivacyPolicy() {
        val locale = Locale.getDefault()
        val url = when(locale.language) {
            Locale.CHINESE.language -> {
                "https://github.com/pantasystem/Milktea/blob/develop/privacy_policy_ch.md"
            }
            Locale.ENGLISH.language -> {
                "https://github.com/pantasystem/Milktea/blob/develop/privacy_policy_en.md"
            }
            Locale.JAPAN.language -> {
                "https://github.com/pantasystem/Milktea/blob/develop/privacy_policy_ja.md"
            }
            else -> {
                "https://github.com/pantasystem/Milktea/blob/develop/privacy_policy_en.md"
            }
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun showTermsOfService() {
        val locale = Locale.getDefault()
        val url = when(locale.language) {
            Locale.CHINESE.language -> {
                "https://github.com/pantasystem/Milktea/blob/develop/terms_of_service_ch.md"
            }
            Locale.ENGLISH.language -> {
                "https://github.com/pantasystem/Milktea/blob/develop/terms_of_service_en.md"
            }
            Locale.JAPAN.language -> {
                "https://github.com/pantasystem/Milktea/blob/develop/terms_of_service_jp.md"
            }
            else -> {
                "https://github.com/pantasystem/Milktea/blob/develop/terms_of_service_en.md"
            }
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}