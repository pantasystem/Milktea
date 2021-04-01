package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.view.auth.AuthFragment
import jp.panta.misskeyandroidclient.view.auth.AuthResultFragment
import jp.panta.misskeyandroidclient.view.auth.Waiting4userAuthorizationFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.lang.IllegalStateException

class AuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        val miCore = application as MiCore
        val authViewModel = ViewModelProvider(this, AuthViewModel.Factory(miCore))[AuthViewModel::class.java]

        lifecycleScope.launchWhenCreated {
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
}