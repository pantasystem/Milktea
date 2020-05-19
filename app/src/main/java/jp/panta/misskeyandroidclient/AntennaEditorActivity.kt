package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.view.antenna.AntennaEditorFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaEditorViewModel

class AntennaEditorActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_ANTENNA = "jp.panta.misskeyandroidclient.AntennaEditorActivity.EXTRA_ANTENNA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antenna_editor)

        val antenna = intent.getSerializableExtra(EXTRA_ANTENNA) as? Antenna?

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.antennaEditorBase, AntennaEditorFragment.newInstance(antenna))
            ft.commit()
        }

        val miCore = applicationContext as MiCore
        miCore.currentAccount.observe(this, Observer { ar ->
            val viewModel = ViewModelProvider(this, AntennaEditorViewModel.Factory(ar, miCore, antenna))[AntennaEditorViewModel::class.java]
            viewModel.selectUserEvent.observe(this, Observer {
                showSearchAndSelectUserActivity()
            })
        })
    }

    fun showSearchAndSelectUserActivity(){

    }
}
