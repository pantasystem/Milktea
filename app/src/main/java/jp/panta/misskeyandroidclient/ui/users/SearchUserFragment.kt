package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.databinding.FragmentSearchUserBinding
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ShowUserDetails
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.model.user.User

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchUserFragment : Fragment(R.layout.fragment_search_user), ShowUserDetails {

    companion object {
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.ui.users.SearchUserFragment"

        @JvmStatic
        fun newInstance(userName: String): SearchUserFragment {
            return SearchUserFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_USER_NAME, userName)
                }
            }
        }
    }

    val mBinding: FragmentSearchUserBinding by dataBinding()

    val viewModel: SearchUserViewModel by viewModels()

    private val toggleFollowViewModel: ToggleFollowViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this) {
            toggleFollowViewModel.toggleFollow(it)
        }
        mBinding.searchUsersView.adapter = adapter
        mBinding.searchUsersView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.userViewDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            mBinding.searchUserSwipeRefresh.isRefreshing = it ?: false
        }
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