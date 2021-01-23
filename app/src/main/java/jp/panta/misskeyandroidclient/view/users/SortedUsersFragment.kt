package jp.panta.misskeyandroidclient.view.users

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.Activities

import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.User
import jp.panta.misskeyandroidclient.putActivity
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.ShowUserDetails
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.SortedUsersViewModel
import kotlinx.android.synthetic.main.fragment_explore_users.*

class SortedUsersFragment : Fragment(R.layout.fragment_explore_users), ShowUserDetails{

    companion object{
        const val EXTRA_EXPLORE_USERS_TYPE = "jp.panta.misskeyandroidclient.viewmodel.users.ExploreUsersViewModel.Type"

        const val EXTRA_ORIGIN = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_ORIGIN"
        const val EXTRA_SORT = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_SORT"
        const val EXTRA_STATE = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_STATE"

        @JvmStatic
        fun newInstance(type: SortedUsersViewModel.Type): SortedUsersFragment {
            return SortedUsersFragment()
                .apply{
                arguments = Bundle().apply{
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
                .apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_ORIGIN, origin)
                    putSerializable(EXTRA_STATE, state)
                    putString(EXTRA_SORT, sort)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = arguments?.getSerializable(EXTRA_EXPLORE_USERS_TYPE) as? SortedUsersViewModel.Type
        val condition = SortedUsersViewModel.UserRequestConditions(
            sort = arguments?.getString(EXTRA_SORT),
            state = arguments?.getSerializable(EXTRA_STATE) as? RequestUser.State?,
            origin = arguments?.getSerializable(EXTRA_ORIGIN) as? RequestUser.Origin?
        )

        val miCore = view.context.applicationContext as MiCore
        val exploreUsersViewModel = ViewModelProvider(this, SortedUsersViewModel.Factory(miCore, type, condition))[SortedUsersViewModel::class.java]
        val toggleFollowViewModel = ViewModelProvider(this, ToggleFollowViewModel.Factory(miCore))[ToggleFollowViewModel::class.java]


        exploreUsersViewModel.isRefreshing.observe(viewLifecycleOwner, {
            exploreUsersSwipeRefresh.isRefreshing = it?: false
        })

        exploreUsersSwipeRefresh.setOnRefreshListener {
            exploreUsersViewModel.loadUsers()
        }

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this, toggleFollowViewModel)
        exploreUsersView.adapter = adapter
        exploreUsersView.layoutManager = LinearLayoutManager(view.context)
        exploreUsersViewModel.users.observe( viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }

    override fun show(user: User?) {
        user?: return

        val intent = Intent(requireContext(), UserDetailActivity::class.java)
        intent.putActivity(Activities.ACTIVITY_IN_APP)


        intent.putExtra(UserDetailActivity.EXTRA_USER_ID, user.id)
        startActivity(intent)
    }
}