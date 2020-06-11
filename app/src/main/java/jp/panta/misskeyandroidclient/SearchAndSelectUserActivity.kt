package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.panta.misskeyandroidclient.databinding.ActivitySearchAndSelectUserBinding
import jp.panta.misskeyandroidclient.view.users.selectable.SelectableUsersAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.search.SearchUserViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.android.synthetic.main.activity_search_and_select_user.*

class SearchAndSelectUserActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_SELECTABLE_MAXIMUM_SIZE = "jp.panta.misskeyandroidclient.EXTRA_SELECTABLE_MAXIMUM_SIZE"
        const val EXTRA_SELECTED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_IDS"
        const val EXTRA_SELECTED_USERS = "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USERS"

        const val EXTRA_ADDED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_ADDED_USER_IDS"
        const val EXTRA_REMOVED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_REMOVED_USER_IDS"
    }

    private var mSelectedUserIds: List<String>? = null

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
        val selectedUserIdList = intent.getStringArrayExtra(EXTRA_SELECTED_USER_IDS)?.toList()?: emptyList()
        mSelectedUserIds = selectedUserIdList

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

        searchUserViewModel.getUsers().observe(this, Observer {
            selectableUsersAdapter.submitList(it)
        })

        selectedUserViewModel.selectedUsers.observe(this, Observer {
            selectedUsersAdapter.submitList(it)
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> setResultFinish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        setResultFinish()
    }

    private fun setResultFinish(){
        val selectedDiff = mSelectedUserViewModel?.getSelectedUserIdsChangedDiff()
        val selectedUsers =
            (mSelectedUserViewModel?.selectedUsers?.value ?: emptyList()).mapNotNull {
                it.user.value
            }
        if(selectedDiff == null){
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        val intent = Intent()

        Log.d("SearchAndSelectAC", "新たに追加:${selectedDiff.added}, 削除:${selectedDiff.removed}")
        intent.putExtra(EXTRA_SELECTED_USER_IDS, selectedDiff.selected.toTypedArray())
        intent.putExtra(EXTRA_ADDED_USER_IDS, selectedDiff.added.toTypedArray())
        intent.putExtra(EXTRA_REMOVED_USER_IDS, selectedDiff.removed.toTypedArray())
        intent.putExtra(EXTRA_SELECTED_USERS, ArrayList(selectedUsers))
        setResult(RESULT_OK, intent)
        finish()

    }


}
