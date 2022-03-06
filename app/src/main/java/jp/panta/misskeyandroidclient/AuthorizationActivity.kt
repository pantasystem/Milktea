package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.model.auth.from
import jp.panta.misskeyandroidclient.ui.auth.AuthFragment
import jp.panta.misskeyandroidclient.ui.auth.AuthResultFragment
import jp.panta.misskeyandroidclient.ui.auth.Waiting4userAuthorizationFragment
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.lang.IllegalStateException

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthorizationActivity : AppCompatActivity() {

    private val mViewModel: AuthViewModel by viewModels()

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