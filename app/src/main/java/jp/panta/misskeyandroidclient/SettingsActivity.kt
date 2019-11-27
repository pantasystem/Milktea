package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.view.settings.SettingAdapter
import jp.panta.misskeyandroidclient.view.settings.activities.SettingAppearanceActivity
import jp.panta.misskeyandroidclient.view.settings.activities.SettingMovementActivity
import jp.panta.misskeyandroidclient.view.settings.activities.TabSettingActivity
import jp.panta.misskeyandroidclient.viewmodel.setting.Group
import jp.panta.misskeyandroidclient.viewmodel.setting.MoveSettingActivityPanel
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_settings)

        setSupportActionBar(settingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val movementSetting = MoveSettingActivityPanel<SettingMovementActivity>(
            titleStringRes = R.string.movement,
            activity = SettingMovementActivity::class.java,
            context = this
        )
        movementSetting.startActivityEventBus.observe(this, Observer {
            startActivity(Intent(this, it))
        })

        val tabSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.nav_setting_tab,
            activity = TabSettingActivity::class.java,
            context = this
        )
        tabSetting.startActivityEventBus.observe(this, Observer {
            startActivity(Intent(this, it))
        })

        val appearanceSetting = MoveSettingActivityPanel(
            titleStringRes = R.string.appearance,
            activity = SettingAppearanceActivity::class.java,
            context = this
        )

        appearanceSetting.startActivityEventBus.observe(this, Observer{
            startActivity(Intent(this, it))
        })

        val group = Group(
            titleStringRes = null,
            context = this,
            items = listOf(movementSetting, tabSetting, appearanceSetting)
        )

        val adapter = SettingAdapter(this)
        setting_list.adapter = adapter
        setting_list.layoutManager = LinearLayoutManager(this)
        adapter.submitList(listOf(group))

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
