package net.pantasystem.milktea.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.auth.viewmodel.app.AppAuthViewModel
import net.pantasystem.milktea.data.infrastructure.auth.Authorization

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authViewModel: AppAuthViewModel,
    onCopyToClipboard: (String) -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onShowTermsOfService: () -> Unit,
    onSignUpButtonClicked: () -> Unit,
) {
    val uiState by authViewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.windowInsetsPadding(
            WindowInsets
                .navigationBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        ),
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                title = {
                    when(uiState.stateType) {
                        is Authorization.Approved -> {
                            Text(stringResource(id = R.string.success))
                        }
                        Authorization.BeforeAuthentication -> {
                            Text(stringResource(R.string.auth))
                        }
                        is Authorization.Finish -> {
                            Text("認証完了")
                        }
                        is Authorization.Waiting4UserAuthorization -> {
                            Text(stringResource(id = R.string.auth_waiting_4_approval))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val stateType = uiState.stateType) {
                Authorization.BeforeAuthentication -> {
                    val password by authViewModel.password.collectAsState()
                    val instanceDomain by authViewModel.instanceDomain.collectAsState()
                    AuthFormScreen(
                        uiState = uiState,
                        password = password,
                        instanceDomain = instanceDomain,
                        onInputInstanceDomain = {
                            authViewModel.instanceDomain.value = it
                        },

                        onInputPassword = {
                            authViewModel.password.value = it
                        },
                        onStartAuthButtonClicked = {
                            authViewModel.auth()
                        },
                        onToggleTermsOfServiceAgreement = authViewModel::onToggleTermsOfServiceAgreement,
                        onTogglePrivacyPolicyAgreement = authViewModel::onTogglePrivacyPolicyAgreement,
                        onShowTermsOfService = onShowTermsOfService,
                        onShowPrivacyPolicy = onShowPrivacyPolicy,
                        onToggleAcceptMastodonAlphaTest = authViewModel::onToggleAcceptMastodonAlphaTest,
                        onSignUpButtonClicked = onSignUpButtonClicked,
                        onBottomReached = authViewModel::onBottomReached
                    )
                }
                is Authorization.Waiting4UserAuthorization -> {
                    Waiting4ApproveScreen(
                        state = stateType,
                        onApprovedButtonClicked = {
                            authViewModel.getAccessToken()
                        },
                        onCopyAuthUrlButtonClicked = {
                            onCopyToClipboard(stateType.generateAuthUrl())
                        }
                    )
                }
                is Authorization.Approved -> {
                    AuthApprovedScreen(
                        state = stateType,
                        onConfirm = {
                            authViewModel.onConfirmAddAccount()
                        }
                    )
                }
                is Authorization.Finish -> {

                }

            }
        }
    }
}