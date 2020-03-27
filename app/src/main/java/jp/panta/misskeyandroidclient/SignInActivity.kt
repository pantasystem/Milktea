package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivitySignInBinding
import jp.panta.misskeyandroidclient.viewmodel.auth.signin.SignInViewModel

class SignInActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_MODE = "jp.panta.misskeyandroidclient.SignInActivity.EXTRA_MODE"
        const val MODE_OVERWRITE = 0
        const val MODE_ADD = 1

        const val EXTRA_INSTANCE_DOMAIN = "jp.panta.misskeyandroidclient.SignInActivity.EXTRA_INSTANCE_DOMAIN"
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.SignInActivity.ExTRA_USER_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivitySignInBinding>(this, R.layout.activity_sign_in)
        setSupportActionBar(binding.signInToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.sign_in)

        val mode = intent.getIntExtra(EXTRA_MODE, MODE_OVERWRITE)
        val instanceDomain = intent.getStringExtra(EXTRA_INSTANCE_DOMAIN)
        val userName = intent.getStringExtra(EXTRA_USER_NAME)

        val miApplication = application as MiApplication
        val viewModel = ViewModelProvider(this, SignInViewModel.Factory(miApplication, mode))[SignInViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        viewModel.instanceDomain.value = instanceDomain?: String()
        viewModel.userName.value = userName?: String()


        viewModel.isValidDomain.observe(this, Observer{
            //Log.d("", "インスタンスの有効性: $it")
            if(!it){
                binding.inputInstanceDomain.error = getString(R.string.invalid_url)
            }
        })

        viewModel.isValidityOfAuth.observe(this, Observer{
            if(!it){
                binding.inputUserName.error = getString(R.string.invalid_pw_id)
                binding.inputPassword.error = getString(R.string.invalid_pw_id)
            }
        })

        viewModel.connectionInformation.observe(this, Observer {

            miApplication.putConnectionInfo(it.first, it.second)

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_sign_in_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
            R.id.menu_auth ->{
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
            R.id.menu_custom_app ->{
                startActivity(Intent(this, CustomAppActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
