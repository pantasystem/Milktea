package jp.panta.misskeyandroidclient

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityAppAuthBinding
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.viewmodel.auth.app.AppAuthViewModel

class AppAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityAppAuthBinding>(this, R.layout.activity_app_auth)
        setSupportActionBar(binding.appAuthToolbar)

        val appAuthViewModel = ViewModelProvider(this, AppAuthViewModel.Factory(CustomAuthStore.newInstance(this)))[AppAuthViewModel::class.java]
        appAuthViewModel.appName.value = getString(R.string.app_name)
        binding.lifecycleOwner = this
        binding.appAuthViewModel = appAuthViewModel
        appAuthViewModel.session.observe(this, Observer {
            it?.let{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
            }
        })

        appAuthViewModel.app.observe(this, Observer{ app ->
            if(app != null){
                Toast.makeText(this, getString(R.string.successfully_created_the_app) + " ${app.name}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
