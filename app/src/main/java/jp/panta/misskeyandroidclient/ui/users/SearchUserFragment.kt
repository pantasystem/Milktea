package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.databinding.FragmentSearchUserBinding
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.ShowUserDetails
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SearchUserFragment : Fragment(R.layout.fragment_search_user), ShowUserDetails{

    companion object{
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.ui.users.SearchUserFragment"
        const val EXTRA_HAS_DETAIL = "jp.panta.misskeyandroidclient.ui.users.HAS_DETAIL"

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

    val mBinding: FragmentSearchUserBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hasDetail = arguments?.getBoolean(EXTRA_HAS_DETAIL)
        val userName = arguments?.getString(EXTRA_USER_NAME)?: ""

        val miCore = requireContext().applicationContext as MiCore
        val viewModel =  ViewModelProvider(this, SearchUserViewModel.Factory(miCore, hasDetail))[SearchUserViewModel::class.java]
        viewModel.userName.value = userName

        val toggleFollowViewModel = ViewModelProvider(this, ToggleFollowViewModel.Factory(miCore))[ToggleFollowViewModel::class.java]

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this, toggleFollowViewModel)
        mBinding.searchUsersView.adapter = adapter
        mBinding.searchUsersView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.getUsers().observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        viewModel.isLoading.observe(viewLifecycleOwner, {
            mBinding.searchUserSwipeRefresh.isRefreshing = it?: false
        })
        mBinding.searchUserSwipeRefresh.setOnRefreshListener {
            viewModel.search()
        }
    }


    override fun show(userId: User.Id?) {
        userId?.let {
            UserDetailActivity.newInstance(requireContext(), userId = userId)
        }
    }
}