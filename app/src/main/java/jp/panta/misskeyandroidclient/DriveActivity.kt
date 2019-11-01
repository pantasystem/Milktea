package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import jp.panta.misskeyandroidclient.view.drive.DriveFragment

class DriveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.content_main, DriveFragment())
            ft.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
