package net.pantasystem.milktea.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_viewmodel.CurrentPageType
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.model.user.User

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val currentPageableViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    private val accountViewModel by viewModels<AccountViewModel>()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    val uiState by accountViewModel.uiState.collectAsState()

                    Scaffold() { paddingValues ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .nestedScroll(rememberNestedScrollInteropConnection())
                        ) {
                            item {
                                when(val account = uiState.currentAccount) {
                                    null -> {
                                        Text(stringResource(id = R.string.unauthorized_error))
                                    }
                                    else -> {
                                        when(val user = uiState.userInfo) {
                                            is User.Detail -> {
                                                AccountInfoLayout(
                                                    isUserNameMain = false,
                                                    userDetail = user,
                                                    account = account,
                                                )
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