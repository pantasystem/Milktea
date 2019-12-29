package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityCustomAppBinding
import jp.panta.misskeyandroidclient.databinding.ItemAccountBinding
import jp.panta.misskeyandroidclient.databinding.ItemAppBinding
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
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
    }

    private val startChoosingAppEventObserver = Observer<Unit>{

    }

    private val createAppEventObserver = Observer<Unit>{
        startActivityForResult(Intent(this, CustomAppCreatorActivity::class.java), REQUEST_CREATE_APP)
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


}
