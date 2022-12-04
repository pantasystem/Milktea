package net.pantasystem.milktea.setting.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.SettingAdapter
import net.pantasystem.milktea.setting.databinding.ActivitySettingsBinding
import net.pantasystem.milktea.setting.viewmodel.MoveSettingActivityPanel
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val binding: ActivitySettingsBinding by dataBinding()

    @Inject
    lateinit var applyTheme: ApplyTheme

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_settings)

        setSupportActionBar(binding.settingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val movementSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.movement,
            activity = SettingMovementActivity::class.java,
            context = this
        )
        movementSetting.startActivityEventBus.observe(this) {
            startActivity(Intent(this, it))
        }

        val tabSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.nav_setting_tab,
            activity = PageSettingActivity::class.java,
            context = this
        )
        tabSetting.startActivityEventBus.observe(this) {
            startActivity(Intent(this, it))
        }

        val securitySetting = MoveSettingActivityPanel(
            titleStringRes = R.string.security_setting,
            activity = SecuritySettingActivity::class.java,
            context = this
        )
        securitySetting.startActivityEventBus.observe(this) {
            startActivity(Intent(this, it))
        }
        val appearanceSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.appearance,
            activity = SettingAppearanceActivity::class.java,
            context = this
        )

        appearanceSetting.startActivityEventBus.observe(this) {
            startActivity(Intent(this, it))
        }

        val reactionSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.reaction,
            activity = ReactionSettingActivity::class.java,
            context = this
        ).apply {
            startActivityEventBus.observe(this@SettingsActivity) {
                startActivity(Intent(this@SettingsActivity, it))
            }
        }

        val clientMuteWordSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.client_word_mute,
            activity = ClientWordFilterSettingActivity::class.java,
            context = this
        ).apply {
            startActivityEventBus.observe(this@SettingsActivity) {
                startActivity(Intent(this@SettingsActivity, it))
            }
        }

        val accountSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.account,
            activity = AccountSettingActivity::class.java,
            context = this
        ).apply {
            startActivityEventBus.observe(this@SettingsActivity) {
                startActivity(Intent(this@SettingsActivity, it))
            }
        }

        val licenseActivitySetting =
            MoveSettingActivityPanel(
                titleStringRes = R.string.license,
                activity = OssLicensesMenuActivity::class.java,
                context = this
            )
        licenseActivitySetting.startActivityEventBus.observe(this) {
            val intent = Intent(this, it)
            intent.putExtra("title", getString(R.string.license))
            startActivity(intent)
        }

        val group = net.pantasystem.milktea.setting.viewmodel.Group(
            titleStringRes = null,
            context = this,
            items = listOf(
                accountSetting,
                movementSetting,
                tabSetting,
                appearanceSetting,
                securitySetting,
                reactionSetting,
                clientMuteWordSetting,
                licenseActivitySetting
            )
        )

        val adapter = SettingAdapter(this)
        binding.settingList.adapter = adapter
        binding.settingList.layoutManager = LinearLayoutManager(this)
        adapter.submitList(listOf(group))

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
