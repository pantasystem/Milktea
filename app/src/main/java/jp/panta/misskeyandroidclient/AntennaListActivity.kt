package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaListViewModel
import kotlinx.android.synthetic.main.activity_antenna_list.*

class AntennaListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antenna_list)
        setSupportActionBar(antennaListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val antennaListViewModel = ViewModelProvider(
            this,
            AntennaListViewModel.Factory(this.applicationContext as MiApplication)
        )[AntennaListViewModel::class.java]

        antennaListViewModel.editAntennaEvent.observe(this , Observer {

        })

        antennaListViewModel.confirmDeletionAntennaEvent.observe(this, Observer {

        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
