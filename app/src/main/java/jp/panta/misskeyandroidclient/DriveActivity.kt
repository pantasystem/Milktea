package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.view.drive.DriveFragment
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory

class DriveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            val viewModel = ViewModelProvider(this, DriveViewModelFactory(it, miApplication))

        })

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
