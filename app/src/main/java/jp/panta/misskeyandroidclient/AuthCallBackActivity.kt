package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.databinding.ActivityAuthCallBackBinding
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.AuthStorage
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.UserKey
import kotlinx.android.synthetic.main.activity_auth_call_back.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthCallBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_auth_call_back)

        val binding: ActivityAuthCallBackBinding = DataBindingUtil.setContentView(this, R.layout.activity_auth_call_back)

        val authStorage = AuthStorage(PreferenceManager.getDefaultSharedPreferences(this))

        val domain  = authStorage.getInstanceDomain()
        val session = authStorage.getSession()

        var accessToken: AccessToken? = null

        if(domain != null && session != null){
            val misskeyAuthAPI = MisskeyAPIServiceBuilder.buildAuthAPI(domain)

            val instance = SecretConstant.getInstances()[domain]
            if(instance == null){
                finish()
                return
            }
            val request = UserKey(appSecret = instance.appSecret, token = session.token)
            Log.d("AuthCallBackActivity", "request: $request, domain: $domain")
            misskeyAuthAPI.getAccessToken(request).enqueue(object :Callback<AccessToken>{
                override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                    Log.d("AuthCallBackActivity", "response: $response")
                    accessToken = response.body()
                    binding.user = response.body()?.user
                }

                override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                    Log.d("AuthCallBackActivity", "アクセストークン取得中に致命的なエラーが発生", t)
                }
            })
            button_ok.setOnClickListener {
                val token = accessToken
                if(token == null){
                    Toast.makeText(this, "Please wait.", Toast.LENGTH_LONG).show()
                }else{
                    val miApplication = application as MiApplication
                    val ci = miApplication.connectionInstancesLiveData.value?.firstOrNull {
                        it.userId == token.user.id
                    }?.apply{
                        state = ConnectionInstance.APP_PROVIDER
                        setAccessToken(token.accessToken, miApplication.mEncryption)
                    }?: ConnectionInstance(instanceBaseUrl = instance.domain,  userId = token.user.id).apply{
                        setAccessToken(token.accessToken, (application as MiApplication).mEncryption)
                    }
                    miApplication.addAccount(ci)
                    finish()

                }
            }

        }


    }
}
