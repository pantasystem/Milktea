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
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ShowUserDetails
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SearchUserFragment : Fragment(R.layout.fragment_search_user), ShowUserDetails {

    companion object{
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.ui.users.SearchUserFragment"

        @JvmStatic
        fun newInstance(userName: String): SearchUserFragment{
            return SearchUserFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_USER_NAME, userName)
                }
            }
        }
    }

    val mBinding: FragmentSearchUserBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = arguments?.getString(EXTRA_USER_NAME)?: ""

        val miCore = requireContext().applicationContext as MiCore
        val viewModel =  ViewModelProvider(this, SearchUserViewModel.Factory(miCore))[SearchUserViewModel::class.java]
        viewModel.userName.value = userName

        val toggleFollowViewModel = ViewModelProvider(this, ToggleFollowViewModel.Factory(miCore))[ToggleFollowViewModel::class.java]

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this, toggleFollowViewModel)
        mBinding.searchUsersView.adapter = adapter
        mBinding.searchUsersView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.users.observe(viewLifecycleOwner, {
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