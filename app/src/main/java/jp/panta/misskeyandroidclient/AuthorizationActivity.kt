package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.data.infrastructure.auth.from
import net.pantasystem.milktea.auth.AuthFragment
import net.pantasystem.milktea.auth.AuthResultFragment
import net.pantasystem.milktea.auth.Waiting4userAuthorizationFragment
import net.pantasystem.milktea.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.lang.IllegalStateException

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthorizationActivity : AppCompatActivity() {

    private val mViewModel: net.pantasystem.milktea.auth.viewmodel.AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        lifecycleScope.launchWhenResumed {
            mViewModel.authorization.collect {
                if(it is Authorization.Finish) {
                    startActivity(Intent(this@AuthorizationActivity, MainActivity::class.java))
                    finish()
                    return@collect
                }
                changeFragment(it)
            }

        }
    }

    /**
     * フラグメントの状態をAuthorizationに合わせて変化させる
     */
    private fun changeFragment(authorization: Authorization) {
        val fragment = when(authorization) {

            is Authorization.BeforeAuthentication -> {
                net.pantasystem.milktea.auth.AuthFragment()
            }
            is Authorization.Waiting4UserAuthorization -> {
                net.pantasystem.milktea.auth.Waiting4userAuthorizationFragment()
            }
            is Authorization.Approved -> {
                net.pantasystem.milktea.auth.AuthResultFragment()
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