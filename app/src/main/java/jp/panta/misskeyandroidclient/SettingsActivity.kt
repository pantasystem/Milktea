package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivitySettingsBinding
import jp.panta.misskeyandroidclient.ui.settings.SettingAdapter
import jp.panta.misskeyandroidclient.ui.settings.activities.*
import jp.panta.misskeyandroidclient.viewmodel.setting.Group
import jp.panta.misskeyandroidclient.viewmodel.setting.MoveSettingActivityPanel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SettingsActivity : AppCompatActivity() {

    private val binding: ActivitySettingsBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_settings)

        setSupportActionBar(binding.settingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val movementSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.movement,
            activity = SettingMovementActivity::class.java,
            context = this
        )
        movementSetting.startActivityEventBus.observe(this, {
            startActivity(Intent(this, it))
        })

        val tabSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.nav_setting_tab,
            activity = PageSettingActivity::class.java,
            context = this
        )
        tabSetting.startActivityEventBus.observe(this, {
            startActivity(Intent(this, it))
        })

        val appearanceSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.appearance,
            activity = SettingAppearanceActivity::class.java,
            context = this
        )

        appearanceSetting.startActivityEventBus.observe(this, {
            startActivity(Intent(this, it))
        })

        val reactionSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.reaction,
            activity = ReactionSettingActivity::class.java,
            context = this
        ).apply{
            startActivityEventBus.observe(this@SettingsActivity, {
                startActivity(Intent(this@SettingsActivity, it))
            })
        }


        val urlPreviewSource = MoveSettingActivityPanel(
            R.string.url_preview,
            UrlPreviewSourceSettingActivity::class.java,
            this
        )
        urlPreviewSource.startActivityEventBus.observe(this, {
            startActivity(Intent(this, it))
        })


        val licenseActivitySetting = MoveSettingActivityPanel(
            titleStringRes = R.string.license,
            activity = OssLicensesMenuActivity::class.java,
            context = this
        )
        licenseActivitySetting.startActivityEventBus.observe(this, {
            val intent = Intent(this, it)
            intent.putExtra("title", getString(R.string.license))
            startActivity(intent)
        })

        val group = Group(
            titleStringRes = null,
            context = this,
            items = listOf(movementSetting, tabSetting, appearanceSetting, urlPreviewSource, reactionSetting, licenseActivitySetting)
        )

        val adapter = SettingAdapter(this)
        binding.settingList.adapter = adapter
        binding.settingList.layoutManager = LinearLayoutManager(this)
        adapter.submitList(listOf(group))

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
