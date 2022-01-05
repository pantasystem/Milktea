package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.panta.misskeyandroidclient.databinding.ActivitySearchAndSelectUserBinding
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.users.selectable.SelectableUsersAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.search.SearchUserViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.io.Serializable

@FlowPreview
@ExperimentalCoroutinesApi
class SearchAndSelectUserActivity : AppCompatActivity() {

    companion object{
        private const val EXTRA_SELECTABLE_MAXIMUM_SIZE = "jp.panta.misskeyandroidclient.EXTRA_SELECTABLE_MAXIMUM_SIZE"
        private const val EXTRA_SELECTED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_IDS"

        const val EXTRA_SELECTED_USER_CHANGED_DIFF = "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_CHANGED_DIFF"

        fun newIntent(context: Context, selectableMaximumSize: Int = Int.MAX_VALUE, selectedUserIds: List<User.Id> = emptyList()) : Intent{
            return Intent(context, SearchAndSelectUserActivity::class.java).apply {
                putExtra(EXTRA_SELECTABLE_MAXIMUM_SIZE, selectableMaximumSize)
                putExtra(EXTRA_SELECTED_USER_IDS, ArrayList<Serializable>(selectedUserIds))
            }
        }
    }

    private var mSelectedUserIds: List<User.Id>? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    private var mSelectedUserViewModel: SelectedUserViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val activitySearchAndSelectUserBinding
                = DataBindingUtil.setContentView<ActivitySearchAndSelectUserBinding>(this, R.layout.activity_search_and_select_user)

        setSupportActionBar(activitySearchAndSelectUserBinding.searchAndSelectUsersToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        BottomSheetBehavior.from(activitySearchAndSelectUserBinding.selectedUsersView.selectedUsersBottomSheet)

        val selectableMaximumSize = intent.getIntExtra(EXTRA_SELECTABLE_MAXIMUM_SIZE, Int.MAX_VALUE)
        val selectedUserIdList =
            (intent.getSerializableExtra(EXTRA_SELECTED_USER_IDS) as? ArrayList<*>)?.mapNotNull {
                it as? User.Id
            }
        mSelectedUserIds = selectedUserIdList
        Log.d(this.localClassName, "selected user ids :$selectedUserIdList")

        val linearLayoutManager = LinearLayoutManager(this)

        val miCore = applicationContext as MiCore


        val selectedUserViewModel =
            ViewModelProvider(this, SelectedUserViewModel.Factory(miCore, selectableMaximumSize, selectedUserIdList, null))[SelectedUserViewModel::class.java]
        val selectableUsersAdapter = SelectableUsersAdapter(selectedUserViewModel, this)

        val searchUserViewModel =
            ViewModelProvider(this, SearchUserViewModel.Factory(miCore, false))[SearchUserViewModel::class.java]
        activitySearchAndSelectUserBinding.usersView.adapter = selectableUsersAdapter
        activitySearchAndSelectUserBinding.searchUserViewModel = searchUserViewModel
        activitySearchAndSelectUserBinding.selectedUserViewModel = selectedUserViewModel
        activitySearchAndSelectUserBinding.usersView.layoutManager = linearLayoutManager
        activitySearchAndSelectUserBinding.lifecycleOwner = this

        val selectedUsersAdapter = SelectableUsersAdapter(selectedUserViewModel, this)
        activitySearchAndSelectUserBinding.selectedUsersView.selectedUserViewModel = selectedUserViewModel
        activitySearchAndSelectUserBinding.selectedUsersView.selectedUsersView.adapter = selectedUsersAdapter
        activitySearchAndSelectUserBinding.selectedUsersView.selectedUsersView.layoutManager = LinearLayoutManager(this)

        searchUserViewModel.getUsers().observe(this, {
            selectableUsersAdapter.submitList(it)
        })

        selectedUserViewModel.selectedUsers.observe(this, {
            selectedUsersAdapter.submitList(it)
        })
        mSelectedUserViewModel = selectedUserViewModel

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> setResultFinish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResultFinish()
    }


    private fun setResultFinish(){
        val selectedDiff = mSelectedUserViewModel?.getSelectedUserIdsChangedDiff()

        if(selectedDiff == null){
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        val intent = Intent()

        Log.d("SearchAndSelectAC", "新たに追加:${selectedDiff.added}, 削除:${selectedDiff.removed}")
        intent.putExtra(EXTRA_SELECTED_USER_CHANGED_DIFF, selectedDiff)
        setResult(RESULT_OK, intent)
        finish()

    }


}
