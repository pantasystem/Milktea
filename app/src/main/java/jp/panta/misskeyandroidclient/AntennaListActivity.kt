package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaListViewModel
import kotlinx.android.synthetic.main.activity_antenna_list.*

class AntennaListActivity : AppCompatActivity() {

    companion object{
        const val REQUEST_ANTENNA_EDITOR_CODE = 239
    }
    private lateinit var mAntennaListViewModel: AntennaListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_antenna_list)
        setSupportActionBar(antennaListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAntennaListViewModel = ViewModelProvider(
            this,
            AntennaListViewModel.Factory(this.applicationContext as MiApplication)
        )[AntennaListViewModel::class.java]


        mAntennaListViewModel.confirmDeletionAntennaEvent.observe(this, Observer {
            confirmDeleteAntenna(it)
        })
        mAntennaListViewModel.editAntennaEvent.observe( this, Observer {
            val intent = Intent(this, AntennaEditorActivity::class.java)
            intent.putExtra(AntennaEditorActivity.EXTRA_ANTENNA, it)
            startActivityForResult(intent, REQUEST_ANTENNA_EDITOR_CODE)
        })

        addAntennaFab.setOnClickListener {
            startActivityForResult(Intent(this, AntennaEditorActivity::class.java), REQUEST_ANTENNA_EDITOR_CODE)
        }

    }

    private fun confirmDeleteAntenna(antenna: Antenna){
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_deletion))
            .setMessage(antenna.name)
            .setPositiveButton(android.R.string.ok){ _,_  ->
                mAntennaListViewModel.deleteAntenna(antenna)
            }
            .setNegativeButton(android.R.string.cancel){ _, _ ->

            }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_ANTENNA_EDITOR_CODE ->{
                if(resultCode == RESULT_OK){
                    mAntennaListViewModel.loadInit()
                }
            }
        }
    }


}
