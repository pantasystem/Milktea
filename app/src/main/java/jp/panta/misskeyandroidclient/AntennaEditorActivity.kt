package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityAntennaEditorBinding
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.ui.antenna.AntennaEditorFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.antenna.viewmodel.AntennaEditorViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class AntennaEditorActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_ANTENNA_ID = "jp.panta.misskeyandroidclient.AntennaEditorActivity.EXTRA_ANTENNA_ID"

        fun newIntent(context: Context, antennaId: Antenna.Id?) : Intent{
            return Intent(context, AntennaEditorActivity::class.java).apply {
                putExtra(EXTRA_ANTENNA_ID, antennaId)
            }
        }
    }

    @FlowPreview
    private var mViewModel: AntennaEditorViewModel? = null

    private lateinit var mBinding: ActivityAntennaEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_antenna_editor)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_antenna_editor)
        setSupportActionBar(mBinding.antennaEditorToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val antennaId = intent.getSerializableExtra(EXTRA_ANTENNA_ID) as? Antenna.Id

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.antennaEditorBase, AntennaEditorFragment.newInstance(antennaId))
            ft.commit()
        }

        val miCore = applicationContext as MiCore
        val viewModel = ViewModelProvider(this, AntennaEditorViewModel.Factory(miCore, antennaId))[AntennaEditorViewModel::class.java]
        this.mViewModel = viewModel
        viewModel.selectUserEvent.observe(this, {
            showSearchAndSelectUserActivity(it)
        })
        viewModel.name.observe(this, {
            supportActionBar?.title = it
        })
        viewModel.antennaRemovedEvent.observe(this, {
            Toast.makeText(this, getString(R.string.remove), Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        })

        viewModel.antennaAddedStateEvent.observe(this, {
            if(it){
                Toast.makeText(this, getString(R.string.success), Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, getString(R.string.failure), Toast.LENGTH_LONG).show()
            }
        })
    }

    @FlowPreview
    private fun showSearchAndSelectUserActivity(userIds: List<User.Id>){
        val intent = SearchAndSelectUserActivity.newIntent(this, selectedUserIds = userIds)
        requestSearchAndUserResult.launch(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                setResult(RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }



    @FlowPreview
    val requestSearchAndUserResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val resultCode = result.resultCode
        if(resultCode == Activity.RESULT_OK && data != null){
            (data.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult)?.let {
                val userNames = it.selectedUsers.map { user ->
                    user.getDisplayUserName()
                }
                mViewModel?.setUserNames(userNames)
            }
        }
    }
}
