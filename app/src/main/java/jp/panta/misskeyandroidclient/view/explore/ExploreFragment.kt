package jp.panta.misskeyandroidclient.view.explore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.viewmodel.explore.Explore
import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.SortedUsersViewModel
import kotlinx.android.synthetic.main.fragment_explore.view.*

class ExploreFragment : Fragment(R.layout.fragment_explore){


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val localExplores = listOf(
            Explore.UserType( getString(R.string.trending_users), SortedUsersViewModel.Type.TRENDING_USER ),
            Explore.UserType( getString(R.string.users_with_recent_activity), SortedUsersViewModel.Type.USERS_WITH_RECENT_ACTIVITY ),
            Explore.UserType( getString(R.string.newly_joined_users), SortedUsersViewModel.Type.NEWLY_JOINED_USERS ),
            Explore.Tag( getString(R.string.trending_tag), SortedHashTagListViewModel.Conditions(RequestHashTagList.Sort().attachedLocalUsers().asc(), isAttachedToLocalUserOnly = true)
            )

        )

        val exploreInLocalAdapter = ExploresAdapter()
        exploreInLocalAdapter.submitList(localExplores)
        view.exploresLocalView.adapter = exploreInLocalAdapter
        view.exploresLocalView.layoutManager = LinearLayoutManager(view.context)

        val remoteExplores = listOf(
            Explore.UserType( getString(R.string.trending_users), SortedUsersViewModel.Type.REMOTE_TRENDING_USER ),
            Explore.UserType( getString(R.string.users_with_recent_activity), SortedUsersViewModel.Type.REMOTE_USERS_WITH_RECENT_ACTIVITY ),
            Explore.UserType( getString(R.string.newly_discovered_users), SortedUsersViewModel.Type.NEWLY_DISCOVERED_USERS ),
            Explore.Tag( getString(R.string.trending_tag), SortedHashTagListViewModel.Conditions(RequestHashTagList.Sort().attachedRemoteUsers().asc(), isAttachedToRemoteUserOnly = true))

        )

        val exploreInRemoteAdapter = ExploresAdapter()
        view.exploresRemoteView.adapter = exploreInRemoteAdapter
        view.exploresRemoteView.layoutManager = LinearLayoutManager(view.context)
        exploreInRemoteAdapter.submitList(remoteExplores)
    }
}