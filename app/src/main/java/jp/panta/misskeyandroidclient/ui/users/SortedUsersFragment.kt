package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.Activities
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.databinding.FragmentExploreUsersBinding
import jp.panta.misskeyandroidclient.putActivity
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ShowUserDetails
import jp.panta.misskeyandroidclient.ui.users.viewmodel.SortedUsersViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.providerViewModel
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@AndroidEntryPoint
class SortedUsersFragment : Fragment(R.layout.fragment_explore_users), ShowUserDetails {

    companion object {
        const val EXTRA_EXPLORE_USERS_TYPE =
            "jp.panta.misskeyandroidclient.viewmodel.users.ExploreUsersViewModel.Type"

        const val EXTRA_ORIGIN = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_ORIGIN"
        const val EXTRA_SORT = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_SORT"
        const val EXTRA_STATE = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_STATE"

        @JvmStatic
        fun newInstance(type: SortedUsersViewModel.Type): SortedUsersFragment {
            return SortedUsersFragment()
                .apply {
                    arguments = Bundle().apply {
                        putSerializable(EXTRA_EXPLORE_USERS_TYPE, type)
                    }
                }
        }

        @JvmStatic
        fun newInstance(
            origin: RequestUser.Origin?,
            sort: String?,
            state: RequestUser.State?
        ): SortedUsersFragment {
            return SortedUsersFragment()
                .apply {
                    arguments = Bundle().apply {
                        putSerializable(EXTRA_ORIGIN, origin)
                        putSerializable(EXTRA_STATE, state)
                        putString(EXTRA_SORT, sort)
                    }
                }
        }
    }

    val mBinding: FragmentExploreUsersBinding by dataBinding()
    private val toggleFollowViewModel: ToggleFollowViewModel by viewModels()


    @Inject
    lateinit var sortedUserViewModelFactory: SortedUsersViewModel.AssistedViewModelFactory

    private val exploreUsersViewModel: SortedUsersViewModel by viewModels {
        val type =
            arguments?.getSerializable(EXTRA_EXPLORE_USERS_TYPE) as? SortedUsersViewModel.Type
        val condition = SortedUsersViewModel.UserRequestConditions(
            sort = arguments?.getString(EXTRA_SORT),
            state = arguments?.getSerializable(EXTRA_STATE) as? RequestUser.State?,
            origin = arguments?.getSerializable(EXTRA_ORIGIN) as? RequestUser.Origin?
        )
        SortedUsersViewModel.providerViewModel(sortedUserViewModelFactory, type, condition)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreUsersViewModel.isRefreshing.observe(viewLifecycleOwner) {
            mBinding.exploreUsersSwipeRefresh.isRefreshing = it ?: false
        }

        mBinding.exploreUsersSwipeRefresh.setOnRefreshListener {
            exploreUsersViewModel.loadUsers()
        }

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this) {
            toggleFollowViewModel.toggleFollow(it)
        }
        mBinding.exploreUsersView.adapter = adapter
        mBinding.exploreUsersView.layoutManager = LinearLayoutManager(view.context)
        exploreUsersViewModel.users.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun show(userId: User.Id?) {
        userId?.let {
            val intent = UserDetailActivity.newInstance(requireContext(), userId = userId)
            intent.putActivity(Activities.ACTIVITY_IN_APP)
            startActivity(intent)
        }
    }
}