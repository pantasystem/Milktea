package net.pantasystem.milktea.user.followrequests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common_android_ui.user.FollowRequestsFragmentFactory
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@AndroidEntryPoint
class FollowRequestsFragment : Fragment() {

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    val viewModel by viewModels<FollowRequestsViewModel>()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.errors.onEach {
            FollowRequestsErrorHandler(requireContext())(it)
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)

        return ComposeView(requireContext()).apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    val uiState by viewModel.uiState.collectAsState()
                    FollowRequestsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(
                                rememberNestedScrollInteropConnection()
                            ),
                        uiState = uiState,
                        onAccept = viewModel::onAccept,
                        onReject = viewModel::onReject,
                        onAvatarClicked = {
                            startActivity(
                                userDetailNavigation.newIntent(UserDetailNavigationArgs.UserId(it))
                            )
                        },
                        onRefresh = viewModel::onRefresh
                    )
                }
            }
        }
    }
}

class FollowRequestFragmentFactoryImpl @Inject constructor() : FollowRequestsFragmentFactory {
    override fun create(): Fragment {
        return FollowRequestsFragment()
    }
}