package jp.panta.misskeyandroidclient

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityCustomAppBinding
import jp.panta.misskeyandroidclient.databinding.ActivityCustomAppCreatorBinding
import jp.panta.misskeyandroidclient.viewmodel.auth.custom.CustomAppCreatorViewModel

class CustomAppCreatorActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_APP_ID = "jp.panta.misskeyandroidclient.CustomAppCreatorActivity.EXTRA_APP_SECRET"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityCustomAppCreatorBinding>(this, R.layout.activity_custom_app_creator)

        setSupportActionBar(binding.appCreatorToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val miApplication = applicationContext as MiApplication
        val ci = miApplication.currentConnectionInstanceLiveData.value

        val viewModel =  ViewModelProvider(this, CustomAppCreatorViewModel.Factory(ci, miApplication))[CustomAppCreatorViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.app.observe(this, Observer {
            intent.putExtra(EXTRA_APP_ID, it.id)
            setResult(Activity.RESULT_OK)
            finish()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home ->{
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
