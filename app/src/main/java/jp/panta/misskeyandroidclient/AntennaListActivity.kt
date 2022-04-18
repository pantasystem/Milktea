package jp.panta.misskeyandroidclient

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityAntennaListBinding
import net.pantasystem.milktea.model.antenna.Antenna
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.antenna.viewmodel.AntennaListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@AndroidEntryPoint
class AntennaListActivity : AppCompatActivity() {


    private val mAntennaListViewModel: AntennaListViewModel by viewModels()

    private lateinit var mBinding: ActivityAntennaListBinding

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_antenna_list)
        setSupportActionBar(mBinding.antennaListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        mAntennaListViewModel.confirmDeletionAntennaEvent.observe(this, {
            confirmDeleteAntenna(it)
        })
        mAntennaListViewModel.editAntennaEvent.observe( this, {
            val intent = AntennaEditorActivity.newIntent(this, it.id)
            requestEditAntennaResult.launch(intent)
        })

        mBinding.addAntennaFab.setOnClickListener {
            requestEditAntennaResult.launch(Intent(this, AntennaEditorActivity::class.java))
        }

    }

    private fun confirmDeleteAntenna(antenna: Antenna){
        MaterialAlertDialogBuilder(this)
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



    private val requestEditAntennaResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        mAntennaListViewModel.loadInit()
    }


}
