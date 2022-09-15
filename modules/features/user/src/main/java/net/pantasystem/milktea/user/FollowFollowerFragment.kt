package net.pantasystem.milktea.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.user.RequestType
import net.pantasystem.milktea.app_store.user.from
import net.pantasystem.milktea.app_store.user.string
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.viewmodel.FollowFollowerViewModel
import net.pantasystem.milktea.user.viewmodel.provideFactory
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FollowFollowerFragment : Fragment() {

    companion object {
        private const val EXTRA_USER_ID =
            "jp.panta.misskeyandroidclient.ui.users.FollowFollowerFragment.EXTRA_USER_ID"
        private const val EXTRA_TYPE =
            "jp.panta.misskeyandroidclient.ui.users.FollowFollowerFragment.EXTRA_TYPE"

        fun newInstance(type: RequestType): FollowFollowerFragment {
            return FollowFollowerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_USER_ID, type.userId)
                    putString(EXTRA_TYPE, type.string())
                }
            }

        }
    }

    @Inject
    lateinit var viewModelFactory: FollowFollowerViewModel.ViewModelAssistedFactory
    private val followFollowerViewModel by viewModels<FollowFollowerViewModel> {
        val strType = arguments?.getString(EXTRA_TYPE) ?: "following"
        val userId = arguments?.getSerializable(EXTRA_USER_ID) as User.Id

        val type = RequestType.from(strType, userId)
        FollowFollowerViewModel.provideFactory(viewModelFactory, type)
    }

    val viewModel: ToggleFollowViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    val state by followFollowerViewModel.state.collectAsState()
                    val users by followFollowerViewModel.users.observeAsState()
                    UserDetailCardPageableList(
                        pageableState = state,
                        users = users ?: emptyList(),
                        isUserNameMain = false,
                        onAction = ::onAction
                    )
                }
            }
        }.rootView
    }

    fun onAction(it: UserDetailCardPageableListAction) {
        when (it) {
            is UserDetailCardPageableListAction.CardAction -> {
                UserCardActionHandler(requireActivity(), viewModel).onAction(it.cardAction)
            }
            UserDetailCardPageableListAction.OnBottomReached -> {
                followFollowerViewModel.loadOld()
            }
            UserDetailCardPageableListAction.Refresh -> {
                followFollowerViewModel.loadInit()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        followFollowerViewModel.loadInit()
    }

}