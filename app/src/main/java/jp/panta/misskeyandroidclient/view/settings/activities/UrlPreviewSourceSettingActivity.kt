package jp.panta.misskeyandroidclient.view.settings.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SettingsActivity
import jp.panta.misskeyandroidclient.setTheme

class UrlPreviewSourceSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_url_preview_source_setting)

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
