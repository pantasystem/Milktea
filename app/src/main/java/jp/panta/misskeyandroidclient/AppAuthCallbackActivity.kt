package jp.panta.misskeyandroidclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.databinding.ActivityAppAuthCallbackBinding
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.account.newAccount
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.UserKey
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthBridge
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AppAuthCallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityAppAuthCallbackBinding>(this, R.layout.activity_app_auth_callback)
        binding.lifecycleOwner = this
        val store = CustomAuthStore.newInstance(this)
        val data = store.getCustomAuthBridge()
        Log.d("AppAuthCallback", "auth: $data")

        if(data != null && data.enabledDateEnd >= Date()){
            val misskeyAPI = MisskeyAPIServiceBuilder.buildAuthAPI(data.instanceDomain)
            misskeyAPI.getAccessToken(UserKey(appSecret = data.secret, token = data.session.token))
                .enqueue(object : Callback<AccessToken>{
                    override fun onResponse(
                        call: Call<AccessToken>,
                        response: Response<AccessToken>
                    ) {
                        val accessToken = response.body()
                        if(accessToken != null){
                            runOnUiThread {
                                binding.user = accessToken.user
                                registerAccount(accessToken, data)
                            }
                        }else{
                            Log.d("AppAuthCallback", "認証情報取得に失敗した")
                            onFailure()
                        }

                    }

                    override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                        Log.e("AppAuthCallback", "get accessToken onFailure", t)
                        onFailure()
                    }
                })
        }else{
            Log.d("AppAuthCallback", "不正な認証")
            onFailure()
        }
        binding.continueAuth.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun registerAccount(accessToken: AccessToken, data: CustomAuthBridge){
        val miApplication = applicationContext as MiApplication

        miApplication.setCurrentAccount(accessToken.newAccount(data.instanceDomain, miApplication.getEncryption(), data.secret))
    }

    private fun onFailure(){
        startActivity(Intent(this, AppAuthActivity::class.java))
        finish()
    }
}
