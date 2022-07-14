package jp.panta.misskeyandroidclient.ui.users

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
import jp.panta.misskeyandroidclient.ui.users.viewmodel.SortedUsersViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.providerViewModel
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import javax.inject.Inject

@AndroidEntryPoint
class SortedUsersFragment : Fragment() {

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

//    val mBinding: FragmentExploreUsersBinding by dataBinding()
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