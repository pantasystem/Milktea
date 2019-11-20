package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.settings.TabSettingFragment

class TabSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_setting)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.setting_tab_content_main, TabSettingFragment())
        ft.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
