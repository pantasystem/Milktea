package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.databinding.ActivityCustomAppCallbackBinding
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.UserKey
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import retrofit2.Call
import java.util.*
import retrofit2.Callback
import retrofit2.Response

class CustomAppAuthCallbackActivity : AppCompatActivity() {

    private var mAccessToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityCustomAppCallbackBinding>(this, R.layout.activity_custom_app_callback)

        val bridge = CustomAuthStore.newInstance(this).getCustomAuthBridge()
        if(bridge == null){
            showFailureMessage(R.string.auth_failed)
            return
        }
        val now = Date()

        if(now.time > bridge.enabledDateEnd.time){
            showFailureMessage(R.string.expired)
            return
        }

        val api = MisskeyAPIServiceBuilder.buildAuthAPI(bridge.instanceDomain)
        val userKey = UserKey(appSecret = bridge.secret, token = bridge.session.token)
        api.getAccessToken(userKey).enqueue(object : Callback<AccessToken>{
            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                val body = response.body()
                if(body == null){
                    showFailureMessage(R.string.auth_failed)
                    return
                }
                binding.user = body.user
                mAccessToken = body


            }

            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                showFailureMessage(R.string.auth_failed)
            }
        })

        binding.authOk.setOnClickListener {
            val token = mAccessToken?: return@setOnClickListener
            val miApplication = application as MiApplication

            val creator = EncryptedConnectionInformation.Creator(miApplication.getEncryption())
            val ci = creator.create(token, bridge)
            miApplication.putConnectionInfo(Account(token.user.id),ci)
            miApplication.accounts.value?.firstOrNull {
                it.account.id == token.user.id
            }?.let{ ac ->
                miApplication.addAndChangeAccount(ac.account)
            }
            finish()
        }

    }

    private fun showFailureMessage(@StringRes res: Int){
        Toast.makeText(this, res, Toast.LENGTH_LONG).show()
        finish()
    }
}
