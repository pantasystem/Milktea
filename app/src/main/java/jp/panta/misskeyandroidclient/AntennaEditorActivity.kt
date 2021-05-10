package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityAntennaEditorBinding
import jp.panta.misskeyandroidclient.model.antenna.Antenna
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.antenna.AntennaEditorFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class AntennaEditorActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_ANTENNA_ID = "jp.panta.misskeyandroidclient.AntennaEditorActivity.EXTRA_ANTENNA_ID"
        private const val REQUEST_SEARCH_AND_SELECT_USER = 110

        fun newIntent(context: Context, antennaId: Antenna.Id?) : Intent{
            return Intent(context, AntennaEditorActivity::class.java).apply {
                putExtra(EXTRA_ANTENNA_ID, antennaId)
            }
        }
    }

    @FlowPreview
    private var mViewModel: AntennaEditorViewModel? = null

    private lateinit var mBinding: ActivityAntennaEditorBinding

    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_antenna_editor)
        mBinding = DataBindingUtil.setContentView<ActivityAntennaEditorBinding>(this, R.layout.activity_antenna_editor)
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

    private fun showSearchAndSelectUserActivity(userIds: List<User.Id>){
        val intent = SearchAndSelectUserActivity.newIntent(this, selectedUserIds = userIds)
        startActivityForResult(intent, REQUEST_SEARCH_AND_SELECT_USER)
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
    @ExperimentalCoroutinesApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_SEARCH_AND_SELECT_USER ->{
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
    }
}
