package net.pantasystem.milktea.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.compose.rememberAsyncImagePainter
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_viewmodel.CurrentPageType
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.model.instance.online.user.count.OnlineUserCountResult
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.activity.FollowFollowerActivity
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : Fragment() {

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    private val currentPageableViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    private val accountViewModel by viewModels<AccountScreenViewModel>()


    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    val uiState by accountViewModel.uiState.collectAsState()

                    Scaffold() { paddingValues ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .nestedScroll(rememberNestedScrollInteropConnection())
                        ) {
                            item {
                                when (val account = uiState.currentAccount) {
                                    null -> {
                                        Text(stringResource(id = R.string.unauthorized_error))
                                    }
                                    else -> {
                                        when (val user = uiState.userInfo) {
                                            is User.Detail -> {
                                                Column(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 14.dp,
                                                            vertical = 8.dp
                                                        )
                                                ) {
                                                    Text(stringResource(id = R.string.account))
                                                    Box(
                                                        Modifier

                                                            .border(
                                                                1.dp,
                                                                MaterialTheme.colors.primary,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                    ) {
                                                        AccountInfoLayout(
                                                            isUserNameMain = false,
                                                            userDetail = user,
                                                            account = account,
                                                            onFollowerCountButtonClicked = {
                                                                requireActivity().startActivity(
                                                                    FollowFollowerActivity.newIntent(
                                                                        requireContext(),
                                                                        user.id,
                                                                        isFollowing = false
                                                                    )
                                                                )
                                                            },
                                                            onFollowingCountButtonClicked = {
                                                                requireActivity().startActivity(
                                                                    FollowFollowerActivity.newIntent(
                                                                        requireContext(),
                                                                        user.id,
                                                                        isFollowing = true
                                                                    )
                                                                )
                                                            }
                                                        )
                                                    }
                                                }

                                            }
                                            else -> {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            item {

                                when (val info = uiState.instanceInfo) {
                                    null -> Unit
                                    else -> {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(stringResource(id = R.string.instance))
                                            Box(
                                                Modifier
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colors.primary,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                            ) {
                                                Column(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                ) {
                                                    Image(
                                                        rememberAsyncImagePainter(info.iconUrl),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(52.dp)
                                                            .clip(
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                    )
                                                    Text(
                                                        info.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                    )
                                                    when (val count = uiState.onlineUserCount) {
                                                        is OnlineUserCountResult.Success -> {
                                                            Text(
                                                                stringResource(
                                                                    id = R.string.online_user_count_message,
                                                                    count.count
                                                                )
                                                            )
                                                        }
                                                        else -> Unit
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }


                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentPageableViewModel.setCurrentPageType(CurrentPageType.Account)
    }
}