package net.pantasystem.milktea.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import net.pantasystem.milktea.model.user.query.from
import net.pantasystem.milktea.user.viewmodel.SortedUsersViewModel
import net.pantasystem.milktea.user.viewmodel.providerViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SortedUsersFragment : Fragment() {

    companion object {

        const val EXTRA_ORIGIN = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_ORIGIN"
        const val EXTRA_SORT = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_SORT"
        const val EXTRA_STATE = "jp.panta.misskeyandroidclient.viewmodel.users.EXTRA_STATE"


        fun newInstance(findUsersQuery: FindUsersQuery): SortedUsersFragment {
            return SortedUsersFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(EXTRA_ORIGIN, findUsersQuery.origin?.origin)
                        putString(EXTRA_SORT, findUsersQuery.sort?.str())
                        putString(EXTRA_STATE, findUsersQuery.state?.state)
                    }
                }
        }
    }


//    val mBinding: FragmentExploreUsersBinding by dataBinding()
    private val toggleFollowViewModel: ToggleFollowViewModel by viewModels()


    @Inject
    lateinit var sortedUserViewModelFactory: SortedUsersViewModel.AssistedViewModelFactory

    private val exploreUsersViewModel: SortedUsersViewModel by viewModels {

        val findUserQuery = FindUsersQuery(
            origin = FindUsersQuery.Origin.from(arguments?.getString(EXTRA_ORIGIN) ?: ""),
            state = FindUsersQuery.State.from(arguments?.getString(EXTRA_STATE) ?: ""),
            sort = FindUsersQuery.OrderBy.from(arguments?.getString(EXTRA_SORT) ?: "")
        )
        SortedUsersViewModel.providerViewModel(sortedUserViewModelFactory, findUserQuery)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    val users by exploreUsersViewModel.users.observeAsState()

                    UserDetailCardList(
                        pageableState = ResultState.Fixed(StateContent.Exist(emptyList())),
                        users = users ?: emptyList(),
                        isUserNameMain = false,
                        onAction = ::onAction
                    )
                }
            }
        }.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreUsersViewModel.loadUsers()

    }

    fun onAction(it: UserDetailCardListAction) {
        UserCardListActionHandler(requireActivity(), toggleFollowViewModel) {
            exploreUsersViewModel.loadUsers()
        }.onAction(it)
    }


}