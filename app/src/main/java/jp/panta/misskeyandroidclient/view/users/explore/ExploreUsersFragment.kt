package jp.panta.misskeyandroidclient.view.users.explore

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.users.FollowableUserListAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.ShowUserDetails
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.explore.ExploreUsersViewModel
import kotlinx.android.synthetic.main.fragment_explore_users.*

class ExploreUsersFragment : Fragment(R.layout.fragment_explore_users), ShowUserDetails{

    companion object{
        const val EXTRA_EXPLORE_USERS_TYPE = "jp.panta.misskeyandroidclient.viewmodel.users.explore.ExploreUsersViewModel.Type"
        fun newInstance(type: ExploreUsersViewModel.Type): ExploreUsersFragment{
            return ExploreUsersFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_EXPLORE_USERS_TYPE, type)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = arguments?.getSerializable(EXTRA_EXPLORE_USERS_TYPE) as ExploreUsersViewModel.Type
        val miCore = view.context.applicationContext as MiCore
        val exploreUsersViewModel = ViewModelProvider(this, ExploreUsersViewModel.Factory(miCore, type))[ExploreUsersViewModel::class.java]
        val toggleFollowViewModel = ViewModelProvider(this, ToggleFollowViewModel.Factory(miCore))[ToggleFollowViewModel::class.java]


        exploreUsersViewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            exploreUsersSwipeRefresh.isRefreshing = it?: false
        })

        exploreUsersSwipeRefresh.setOnRefreshListener {
            exploreUsersViewModel.loadUsers()
        }

        val adapter = FollowableUserListAdapter(viewLifecycleOwner, this, toggleFollowViewModel)
        exploreUsersView.adapter = adapter
        exploreUsersView.layoutManager = LinearLayoutManager(view.context)
        exploreUsersViewModel.users.observe( viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    override fun show(user: User?) {
        user?: return

        val intent = Intent(requireContext(), UserDetailActivity::class.java)
        intent.putExtra(UserDetailActivity.EXTRA_USER_ID, user.id)
        startActivity(intent)
    }
}