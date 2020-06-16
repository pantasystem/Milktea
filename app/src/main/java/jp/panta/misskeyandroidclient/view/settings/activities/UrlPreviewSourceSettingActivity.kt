package jp.panta.misskeyandroidclient.view.settings.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SettingsActivity
import jp.panta.misskeyandroidclient.databinding.ActivityUrlPreviewSourceSettingBinding
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.viewmodel.setting.url.UrlPreviewSourceSettingViewModel

class UrlPreviewSourceSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityUrlPreviewSourceSettingBinding>(this, R.layout.activity_url_preview_source_setting)

        val miApplication = applicationContext as MiApplication
        val viewModel = ViewModelProvider(this, UrlPreviewSourceSettingViewModel.Factory(miApplication))[UrlPreviewSourceSettingViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                val settings = Intent(this, SettingsActivity::class.java)
                if(shouldUpRecreateTask(settings)){

                }else{

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
