package net.pantasystem.milktea.antenna

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.antenna.databinding.ActivityAntennaListBinding
import net.pantasystem.milktea.antenna.viewmodel.AntennaListViewModel
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.AntennaNavigation
import net.pantasystem.milktea.common_navigation.AntennaNavigationArgs
import net.pantasystem.milktea.model.antenna.Antenna
import javax.inject.Inject

@AndroidEntryPoint
class AntennaListActivity : AppCompatActivity() {


    private val mAntennaListViewModel: AntennaListViewModel by viewModels()

    private lateinit var mBinding: ActivityAntennaListBinding

    @Inject
    lateinit var applyTheme: ApplyTheme

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_antenna_list)
        setSupportActionBar(mBinding.antennaListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        mAntennaListViewModel.confirmDeletionAntennaEvent.onEach {
            confirmDeleteAntenna(it)
        }.flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
        mAntennaListViewModel.editAntennaEvent.onEach {
            val intent = AntennaEditorActivity.newIntent(this, it.id)
            requestEditAntennaResult.launch(intent)
        }.flowWithLifecycle(lifecycle).launchIn(lifecycleScope)

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

class AntennaNavigationImpl @Inject constructor(
    val activity: Activity
): AntennaNavigation {
    override fun newIntent(args: AntennaNavigationArgs): Intent {
        return Intent(activity, AntennaListActivity::class.java).apply {
            putExtra(AntennaListViewModel.EXTRA_SPECIFIED_ACCOUNT_ID, args.specifiedAccountId)
            putExtra(AntennaListViewModel.EXTRA_ADD_TAB_TO_ACCOUNT_ID, args.addTabToAccountId)
        }
    }
}