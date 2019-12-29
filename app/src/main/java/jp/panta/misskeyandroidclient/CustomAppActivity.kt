package jp.panta.misskeyandroidclient

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityCustomAppBinding>(this, R.layout.activity_custom_app)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.customAppToolbar)

        val miApplication = applicationContext as MiApplication

        val accountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication.connectionInstanceDao!!))[AccountViewModel::class.java]
        binding.accountViewModel = accountViewModel

        val accountBinding = DataBindingUtil.inflate<ItemAccountBinding>(
            LayoutInflater.from(binding.currentAccountView.context),
            R.layout.item_account,
            binding.currentAccountView,
            true
        )
        accountBinding.accountViewModel = accountViewModel
        accountBinding.lifecycleOwner = this
        initAccountObserver(accountViewModel)


        //accountBinding.lifecycleOwner = this


        val appBinding = DataBindingUtil.inflate<ItemAppBinding>(
            LayoutInflater.from(binding.currentAppView.context),
            R.layout.item_app,
            binding.currentAppView,
            true
        )
        appBinding.lifecycleOwner = this

        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {ci ->
            val customAppViewModel = ViewModelProvider(this, CustomAppViewModel.Factory(miApplication))[CustomAppViewModel::class.java]
            customAppViewModel.selectedApp.observe(this, Observer {
                Log.d("CustomAppActivity", "選択中のアプリ:${it.name}")
                appBinding.app = it
            })
            customAppViewModel.account.observe(this, Observer {account ->
                if(account != null){
                    val viewData = AccountViewData(connectionInstance = miApplication.currentConnectionInstanceLiveData.value!!,user =  account)
                    accountBinding.accountViewData = viewData
                }
            })

            customAppViewModel.misskeyAPI = miApplication.misskeyAPIService!!

        })




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


}
