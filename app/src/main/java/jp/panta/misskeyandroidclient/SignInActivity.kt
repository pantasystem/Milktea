package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivitySignInBinding
import jp.panta.misskeyandroidclient.viewmodel.auth.signin.SignInViewModel

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySignInBinding>(this, R.layout.activity_sign_in)

        val miApplication = application as MiApplication
        val viewModel = ViewModelProvider(this, SignInViewModel.Factory(miApplication.connectionInstanceDao!!, miApplication.encryption))[SignInViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.i.observe(this, Observer {
            Log.d("", it)
        })

        viewModel.isValidDomain.observe(this, Observer{
            Log.d("", "インスタンスの有効性: $it")
        })

        /*viewModel.me.observe(this, Observer {
            if(it != null){
                finish()
            }
        })*/

        viewModel.connectionInstance.observe(this, Observer {
            (application as MiApplication).addAccount(it)
            finish()
        })
    }
}
