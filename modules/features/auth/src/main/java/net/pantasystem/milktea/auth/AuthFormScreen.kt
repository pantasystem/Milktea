package net.pantasystem.milktea.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.auth.viewmodel.app.AuthUiState
import net.pantasystem.milktea.auth.viewmodel.app.AuthUserInputState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import java.util.*

@Composable
fun AuthFormScreen(
    modifier: Modifier = Modifier,
    uiState: AuthUiState,
    password: String,
    instanceDomain: String,
    appName: String,
    clientId: String,
    onInputInstanceDomain: (String) -> Unit,
    onInputAppName: (String) -> Unit,
    onInputPassword: (String) -> Unit,
    onStartAuthButtonClicked: () -> Unit,
) {
    Column(
        modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,

        ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.https))
                OutlinedTextField(
                    instanceDomain,
                    onValueChange = onInputInstanceDomain,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    label = {
                        Text(stringResource(R.string.instance_domain))
                    }
                )
                IconButton(
                    onClick = {
                        onInputInstanceDomain("")
                    },
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "clear instance domain")
                }
            }
            Spacer(Modifier.height(8.dp))
            if (uiState.formState.isIdPassword) {
                OutlinedTextField(
                    password,
                    onInputPassword,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    label = {
                        Text(stringResource(R.string.password))
                    }
                )
            } else {
                OutlinedTextField(
                    appName,
                    onInputAppName,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    label = {
                        Text(stringResource(R.string.custom_app_name))
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            if (uiState.isProgress) {
                CircularProgressIndicator()
            }

        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Button(
                onClick = onStartAuthButtonClicked,
                enabled = uiState.metaState is ResultState.Fixed && uiState.metaState.content is StateContent.Exist
            ) {
                Text(stringResource(R.string.start_auth))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(clientId)
        }

    }
}

@Preview
@Composable
fun Preview_AuthFormScreen() {
    MaterialTheme {
        Scaffold {
            AuthFormScreen(
                Modifier
                    .padding(it)
                    .fillMaxSize(),
                onInputAppName = {},
                onInputInstanceDomain = {},
                onInputPassword = {},
                onStartAuthButtonClicked = {},
                appName = "",
                instanceDomain = "",
                password = "",
                clientId = "${UUID.randomUUID()}",
                uiState = AuthUiState(
                    formState = AuthUserInputState("", "", "", ""),
                    metaState = ResultState.Loading(StateContent.NotExist()),
                    stateType = Authorization.BeforeAuthentication
                ),
            )
        }
    }
}