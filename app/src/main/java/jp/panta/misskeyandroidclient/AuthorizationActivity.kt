package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthBridge
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.view.auth.AuthFragment
import jp.panta.misskeyandroidclient.view.auth.AuthResultFragment
import jp.panta.misskeyandroidclient.view.auth.Waiting4userAuthorizationFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.lang.IllegalStateException

class AuthorizationActivity : AppCompatActivity() {

    lateinit var mViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)


        val miCore = application as MiCore
        val authViewModel = ViewModelProvider(this, AuthViewModel.Factory(miCore))[AuthViewModel::class.java]
        mViewModel = authViewModel

        lifecycleScope.launchWhenResumed {
            authViewModel.authorization.collect {
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

        if(callbackToken?.isNotBlank() == true) {
            authStore.getCustomAuthBridge()?.let {
                val a = Authorization.Waiting4UserAuthorization(
                    instanceBaseURL = it.instanceDomain,
                    session = it.session,
                    appSecret = it.secret,
                    viaName = it.viaName
                )
                mViewModel.setState(a)
                mViewModel.getAccessToken()
            }
        }
    }
}