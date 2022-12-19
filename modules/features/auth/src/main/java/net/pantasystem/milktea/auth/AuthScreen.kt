package net.pantasystem.milktea.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.auth.viewmodel.app.AuthUiState
import net.pantasystem.milktea.data.infrastructure.auth.Authorization

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authUiState: AuthUiState
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.auth))
                }
            )
        }
    ) { paddingValues ->
        Box(
            Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (val stateType = authUiState.stateType) {
                Authorization.BeforeAuthentication -> {
                    AuthFormScreen()
                }
                is Authorization.Waiting4UserAuthorization -> {
                    Waiting4ApproveScreen()
                }
                is Authorization.Approved -> {
                    AuthApprovedScreen()
                }
                is Authorization.Finish -> {

                }

            }
        }
    }
}