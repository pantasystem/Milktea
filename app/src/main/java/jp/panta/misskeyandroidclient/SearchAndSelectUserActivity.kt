package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val activitySearchAndSelectUserBinding
                = DataBindingUtil.setContentView<ActivitySearchAndSelectUserBinding>(this, R.layout.activity_search_and_select_user)

        setSupportActionBar(activitySearchAndSelectUserBinding.searchAndSelectUsersToolbar)

        BottomSheetBehavior.from(activitySearchAndSelectUserBinding.selectedUsersView.selectedUsersBottomSheet)

        val selectableMaximumSize = 20//intent.getIntExtra(EXTRA_SELECTABLE_MAXIMUM_SIZE, 1)


        val linearLayoutManager = LinearLayoutManager(this)

        val miCore = applicationContext as MiCore
        miCore.currentAccount.observe(this, Observer{ ar ->
            val searchAndSelectUserViewModel = ViewModelProvider(
                this,
                SearchAndSelectUserViewModel.Factory(
                    ar,
                    miCore,
                    selectableMaximumSize
                ))[SearchAndSelectUserViewModel::class.java]


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
}
