package jp.panta.misskeyandroidclient.ui.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityUrlPreviewSourceSettingBinding
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.url.UrlPreviewSourceSettingViewModel

@AndroidEntryPoint
class UrlPreviewSourceSettingActivity : AppCompatActivity() {

    val viewModel: UrlPreviewSourceSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityUrlPreviewSourceSettingBinding>(this, R.layout.activity_url_preview_source_setting)
        setSupportActionBar(binding.urlPreviewSrcSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
