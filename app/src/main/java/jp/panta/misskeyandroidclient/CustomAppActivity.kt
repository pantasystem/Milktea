package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityCustomAppBinding
import jp.panta.misskeyandroidclient.databinding.ItemAccountBinding
import jp.panta.misskeyandroidclient.databinding.ItemAppBinding
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.auth.AppSelectDialog
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewData
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import jp.panta.misskeyandroidclient.viewmodel.auth.custom.CustomAppViewModel

class CustomAppActivity : AppCompatActivity() {

    private val REQUEST_CREATE_APP = 114

    private var mCustomAppViewModel: CustomAppViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityCustomAppBinding>(this, R.layout.activity_custom_app)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.customAppToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.custom_app)

        val miApplication = applicationContext as MiApplication

        val accountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication.connectionInstanceDao!!))[AccountViewModel::class.java]
        binding.accountViewModel = accountViewModel

        binding.addAccount.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
        }

        initAccountObserver(accountViewModel)


        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            val customAppViewModel = ViewModelProvider(this, CustomAppViewModel.Factory(miApplication))[CustomAppViewModel::class.java]
            mCustomAppViewModel = customAppViewModel
            binding.customAppViewModel = customAppViewModel

            customAppViewModel.misskeyAPI = miApplication.misskeyAPIService!!
            initAppObserver(customAppViewModel)


        })




    }

    private fun initAppObserver(customAppViewModel: CustomAppViewModel){
        customAppViewModel.startChoosingAppEvent.removeObserver(startChoosingAppEventObserver)
        customAppViewModel.startChoosingAppEvent.observe(this, startChoosingAppEventObserver)

        customAppViewModel.createAppEvent.removeObserver(createAppEventObserver)
        customAppViewModel.createAppEvent.observe(this, createAppEventObserver)

        customAppViewModel.session.removeObserver(sessionObserver)
        customAppViewModel.session.observe(this, sessionObserver)

        customAppViewModel.isSignInRequiredEvent.removeObserver(isSignInRequiredObserver)
        customAppViewModel.isSignInRequiredEvent.observe(this, isSignInRequiredObserver)
    }

    private val startChoosingAppEventObserver = Observer<Unit>{
        runOnUiThread {
            val dialog = AppSelectDialog()
            dialog.show(supportFragmentManager, "CustomAppActivity")
        }
    }

    private val createAppEventObserver = Observer<Unit>{
        startActivityForResult(Intent(this, CustomAppCreatorActivity::class.java), REQUEST_CREATE_APP)
    }

    private val sessionObserver = Observer<Session>{
        Log.d("CustomAppActivity", "sessionを受信した")
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
        finish()
    }

    private val isSignInRequiredObserver = Observer<Boolean>{
        if(it){
            Log.d("CustomAppActivity", "認証が必要なためSignInActivityを起動します")
            runOnUiThread {
                Toast.makeText(this, getString(R.string.auth_required), Toast.LENGTH_LONG).show()
                val intent = Intent(this, SignInActivity::class.java)
                intent.putExtra(SignInActivity.EXTRA_MODE, SignInActivity.MODE_ADD)
                intent.putExtra(SignInActivity.EXTRA_INSTANCE_DOMAIN, mCustomAppViewModel?.currentConnectionInstanceLiveData?.value?.instanceBaseUrl?: "")
                intent.putExtra(SignInActivity.EXTRA_USER_NAME, mCustomAppViewModel?.account?.value?.user?.userName?: "")
                startActivity(intent)
            }
        }
    }

    private fun initAccountObserver(accountViewModel: AccountViewModel){
        accountViewModel.switchAccount.removeObserver(switchAccountButtonObserver)
        accountViewModel.switchAccount.observe(this,  switchAccountButtonObserver)

        accountViewModel.switchTargetConnectionInstance.removeObserver(switchAccountObserver)
        accountViewModel.switchTargetConnectionInstance.observe(this, switchAccountObserver)
    }

    private val switchAccountButtonObserver = Observer<Int>{
        runOnUiThread{
            val dialog = AccountSwitchingDialog()
            dialog.show(supportFragmentManager, "CustomAppActivity")
        }
    }

    private val switchAccountObserver = Observer<ConnectionInstance>{
        (application as MiApplication).switchAccount(it)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CREATE_APP ->{
                if(resultCode == Activity.RESULT_OK && data != null){
                    mCustomAppViewModel?.setApp(intent.getStringExtra(CustomAppCreatorActivity.EXTRA_APP_ID))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_custom_app_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
            R.id.menu_auth ->{
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
            R.id.menu_sign_in ->{
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
