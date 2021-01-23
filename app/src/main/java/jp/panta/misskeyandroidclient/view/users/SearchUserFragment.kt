package jp.panta.misskeyandroidclient.view.users

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.api.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.ShowUserDetails
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.search.SearchUserViewModel
import kotlinx.android.synthetic.main.fragment_search_user.*

class SearchUserFragment : Fragment(R.layout.fragment_search_user), ShowUserDetails{

    companion object{
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.view.users.SearchUserFragment"
        const val EXTRA_HAS_DETAIL = "jp.panta.misskeyandroidclient.view.users.HAS_DETAIL"

        @JvmStatic
        fun newInstance(userName: String, hasDetail: Boolean): SearchUserFragment{
            return SearchUserFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_USER_NAME, userName)
                    putBoolean(EXTRA_HAS_DETAIL, hasDetail)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val hasDetail = arguments?.getBoolean(EXTRA_HAS_DETAIL)
        val userName = arguments?.getString(EXTRA_USER_NAME)?: ""

        val miCore = requireContext().applicationContext as MiCore
        val viewModel =  ViewModelProvider(this, SearchUserViewModel.Factory(miCore, hasDetail))[SearchUserViewModel::class.java]
        viewModel.userName.value = userName

        val toggleFollowViewModel = ViewModelProvider(this, ToggleFollowViewModel.Factory(miCore))[ToggleFollowViewModel::class.java]

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this, toggleFollowViewModel)
        searchUsersView.adapter = adapter
        searchUsersView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.getUsers().observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        viewModel.isLoading.observe(viewLifecycleOwner, {
            searchUserSwipeRefresh.isRefreshing = it?: false
        })
        searchUserSwipeRefresh.setOnRefreshListener {
            viewModel.search()
        }
    }


    override fun show(user: User?) {

    }
}