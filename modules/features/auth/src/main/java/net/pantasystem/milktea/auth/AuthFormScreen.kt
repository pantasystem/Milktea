package net.pantasystem.milktea.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import net.pantasystem.milktea.auth.viewmodel.app.AuthUiState
import net.pantasystem.milktea.auth.viewmodel.app.AuthUserInputState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd
import net.pantasystem.milktea.data.infrastructure.auth.Authorization

@Composable
fun AuthFormScreen(
    modifier: Modifier = Modifier,
    uiState: AuthUiState,
    password: String,
    instanceDomain: String,
    onInputInstanceDomain: (String) -> Unit,
    onInputPassword: (String) -> Unit,
    onStartAuthButtonClicked: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onShowTermsOfService: () -> Unit,
    onTogglePrivacyPolicyAgreement: (Boolean) -> Unit,
    onToggleTermsOfServiceAgreement: (Boolean) -> Unit,
    onToggleAcceptMastodonAlphaTest: (Boolean) -> Unit,
    onSignUpButtonClicked: () -> Unit,
    onBottomReached: () -> Unit,
) {

    Column(
        modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                instanceDomain,
                onValueChange = onInputInstanceDomain,
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 1,
                label = {
                    Text(stringResource(R.string.auth_instance_domain))
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onInputInstanceDomain("")
                        },
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "clear instance domain")
                    }
                }
            )
            if (uiState.formState.isIdPassword) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    password,
                    onInputPassword,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    label = {
                        Text(stringResource(R.string.password))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                )
            }


            if (uiState.isProgress) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }

        }

        FilteredInstances(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            uiState = uiState,
            onInputInstanceDomain = onInputInstanceDomain,
            onBottomReached = onBottomReached
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.End,
        ) {
            if (uiState.metaState is ResultState.Fixed
                && uiState.metaState.content is StateContent.Exist
            ) {
                AgreementLayout(
                    uiState = uiState,
                    onShowPrivacyPolicy = onShowPrivacyPolicy,
                    onShowTermsOfService = onShowTermsOfService,
                    onTogglePrivacyPolicyAgreement = onTogglePrivacyPolicyAgreement,
                    onToggleTermsOfServiceAgreement = onToggleTermsOfServiceAgreement,
                    onToggleAcceptMastodonAlphaTest = onToggleAcceptMastodonAlphaTest
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onSignUpButtonClicked,
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text(stringResource(R.string.auth_sign_up))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onStartAuthButtonClicked,
                    enabled = uiState.metaState is ResultState.Fixed
                            && uiState.metaState.content is StateContent.Exist
                            && uiState.formState.isPrivacyPolicyAgreement
                            && uiState.formState.isTermsOfServiceAgreement
                            && (!uiState.isMastodon || uiState.formState.isAcceptMastodonAlphaTest),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text(stringResource(R.string.start_auth))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

    }
}

@Composable
private fun AgreementLayout(
    uiState: AuthUiState,
    onShowPrivacyPolicy: () -> Unit,
    onShowTermsOfService: () -> Unit,
    onTogglePrivacyPolicyAgreement: (Boolean) -> Unit,
    onToggleTermsOfServiceAgreement: (Boolean) -> Unit,
    onToggleAcceptMastodonAlphaTest: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = onShowTermsOfService
        ) {
            Text(stringResource(id = R.string.auth_terms_of_service_agreeation))
        }
        Switch(
            checked = uiState.formState.isTermsOfServiceAgreement,
            onCheckedChange = onToggleTermsOfServiceAgreement
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            modifier = Modifier.clickable(onClick = onShowPrivacyPolicy),
            onClick = onShowPrivacyPolicy,
        ) {
            Text(stringResource(id = R.string.auth_privacy_policy_agreeation))
        }
        Switch(
            checked = uiState.formState.isPrivacyPolicyAgreement,
            onCheckedChange = onTogglePrivacyPolicyAgreement
        )
    }

    if (uiState.isMastodon) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(id = R.string.auth_accpet_mastodon_alpha_test),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Switch(
                checked = uiState.formState.isAcceptMastodonAlphaTest,
                onCheckedChange = onToggleAcceptMastodonAlphaTest,
            )
        }
    }

}

@Composable
private fun FilteredInstances(
    modifier: Modifier = Modifier,
    uiState: AuthUiState,
    onInputInstanceDomain: (String) -> Unit,
    onBottomReached: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.isScrolledToTheEnd()
        }.distinctUntilChanged().collect {
            if (it) {
                onBottomReached()
            }
        }
    }
    val instances = uiState.misskeyInstanceInfosResponse
    LazyColumn(
        modifier,
        state = listState,
    ) {
        items(instances) { instance ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onInputInstanceDomain(instance.url)
                    }
            ) {
                Text(
                    instance.url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
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
                onInputInstanceDomain = {},
                onInputPassword = {},
                onStartAuthButtonClicked = {},
                instanceDomain = "",
                password = "",
                uiState = AuthUiState(
                    formState = AuthUserInputState(
                        "",
                        "",
                        "",
                        "",
                        isPrivacyPolicyAgreement = false,
                        isTermsOfServiceAgreement = false,
                        isAcceptMastodonAlphaTest = false,
                    ),
                    metaState = ResultState.Loading(StateContent.NotExist()),
                    stateType = Authorization.BeforeAuthentication,
                    misskeyInstanceInfosResponse = emptyList(),
                ),
                onShowPrivacyPolicy = {},
                onShowTermsOfService = {},
                onTogglePrivacyPolicyAgreement = {},
                onToggleTermsOfServiceAgreement = {},
                onToggleAcceptMastodonAlphaTest = {},
                onSignUpButtonClicked = {},
                onBottomReached = {}
            )
        }
    }
}