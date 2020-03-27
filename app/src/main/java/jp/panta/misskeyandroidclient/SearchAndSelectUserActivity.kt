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
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SearchAndSelectUserViewModel
import kotlinx.android.synthetic.main.activity_search_and_select_user.*

class SearchAndSelectUserActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_SELECTABLE_MAXIMUM_SIZE = "jp.panta.misskeyandroidclient.EXTRA_SELECTABLE_MAXIMUM_SIZE"
        const val EXTRA_SELECTED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_IDS"

        const val EXTRA_ADDED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_ADDED_USER_IDS"
        const val EXTRA_REMOVED_USER_IDS = "jp.panta.misskeyandroidclient.EXTRA_REMOVED_USER_IDS"
    }

    private var mSearchAndSelectUserViewModel: SearchAndSelectUserViewModel? = null
    private var mSelectedUserIds: List<String>? = null

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
        miCore.currentAccount.observe(this, Observer{ ar ->
            val searchAndSelectUserViewModel = ViewModelProvider(
                this,
                SearchAndSelectUserViewModel.Factory(
                    ar,
                    miCore,
                    selectableMaximumSize,
                    selectedUserIdList
                ))[SearchAndSelectUserViewModel::class.java]

            mSearchAndSelectUserViewModel = searchAndSelectUserViewModel

            val selectableUsersAdapter = SelectableUsersAdapter(searchAndSelectUserViewModel, this)
            activitySearchAndSelectUserBinding.usersView.adapter = selectableUsersAdapter
            activitySearchAndSelectUserBinding.searchSelectViewModel = searchAndSelectUserViewModel
            activitySearchAndSelectUserBinding.usersView.layoutManager = linearLayoutManager
            activitySearchAndSelectUserBinding.lifecycleOwner = this

            val selectedUsersAdapter = SelectableUsersAdapter(searchAndSelectUserViewModel, this)
            activitySearchAndSelectUserBinding.selectedUsersView.searchAndSelectUserViewModel = searchAndSelectUserViewModel
            activitySearchAndSelectUserBinding.selectedUsersView.selectedUsersView.adapter = selectedUsersAdapter
            activitySearchAndSelectUserBinding.selectedUsersView.selectedUsersView.layoutManager = LinearLayoutManager(this)


            searchAndSelectUserViewModel.searchResultUsers.observe(this, Observer{ list ->
                selectableUsersAdapter.submitList(list)
            })

            searchAndSelectUserViewModel.selectedUsers.observe(this, Observer{ selectedUsers ->
                selectedUsersAdapter.submitList(selectedUsers)

            })


        })


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> setResultFinish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        setResultFinish()
    }

    private fun setResultFinish(){
        val intent = Intent()
        val selectedDiff = mSearchAndSelectUserViewModel?.getSelectedUserIdsChangedDiff()
        if(selectedDiff == null){
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        intent.putExtra(EXTRA_SELECTED_USER_IDS, selectedDiff.selected.toTypedArray())
        intent.putExtra(EXTRA_ADDED_USER_IDS, selectedDiff.added.toTypedArray())
        intent.putExtra(EXTRA_REMOVED_USER_IDS, selectedDiff.removed.toTypedArray())
        setResult(RESULT_OK, intent)
        finish()

    }


}
