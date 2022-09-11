package net.pantasystem.milktea.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.auth.viewmodel.AuthViewModel
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.data.infrastructure.auth.from
import javax.inject.Inject


class AuthorizationNavigationImpl @Inject constructor(
    val activity: Activity
) : AuthorizationNavigation {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, AuthorizationActivity::class.java)
    }
}
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthorizationActivity : AppCompatActivity() {

    private val mViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var mainNavigation: MainNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mViewModel.authorization.collect {
                    if(it is Authorization.Finish) {
                        startActivity(mainNavigation.newIntent(Unit))
                        finish()
                        return@collect
                    }
                    changeFragment(it)
                }
            }


        }
    }

    /**
     * フラグメントの状態をAuthorizationに合わせて変化させる
     */
    private fun changeFragment(authorization: Authorization) {
        val fragment = when(authorization) {

            is Authorization.BeforeAuthentication -> {
                AuthFragment()
            }
            is Authorization.Waiting4UserAuthorization -> {
                Waiting4userAuthorizationFragment()
            }
            is Authorization.Approved -> {
                AuthResultFragment()
            }
            is Authorization.Finish -> {
                throw IllegalStateException("Finishは期待されていません")
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_base, fragment)
        ft.commit()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val authStore = CustomAuthStore.newInstance(this)
        val callbackToken = intent?.data?.getQueryParameter("token")
        val callbackMastodonCode = intent?.data?.getQueryParameter("code")

        if(callbackToken?.isNotBlank() == true) {
            authStore.getCustomAuthBridge()?.let {
                val state = Authorization.Waiting4UserAuthorization.from(it)
                mViewModel.setState(state)
                mViewModel.getAccessToken()

            }
        } else if (callbackMastodonCode?.isNotBlank() == true) {
            authStore.getCustomAuthBridge()?.let {
                val state = Authorization.Waiting4UserAuthorization.from(it)
                mViewModel.setState(state)
                mViewModel.getAccessToken(callbackMastodonCode)
            }
        }
    }
}