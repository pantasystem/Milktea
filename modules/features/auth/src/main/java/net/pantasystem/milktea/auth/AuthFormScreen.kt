package net.pantasystem.milktea.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.auth.viewmodel.app.AuthUiState
import net.pantasystem.milktea.auth.viewmodel.app.AuthUserInputState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
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
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null)
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
            instanceDomain = instanceDomain,
            onInputInstanceDomain = onInputInstanceDomain,
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
            Text(stringResource(id = R.string.terms_of_service_agreeation))
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
            Text(stringResource(id = R.string.privacy_policy_agreeation))
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
                stringResource(id = R.string.accpet_mastodon_alpha_test),
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
    instanceDomain: String,
    onInputInstanceDomain: (String) -> Unit,
) {
    val instances = remember(uiState.misskeyInstanceInfosResponse, uiState.formState) {
        uiState.misskeyInstanceInfosResponse?.instancesInfos?.filter {
            it.meta.uri.contains(instanceDomain) || it.name.contains(instanceDomain)
        } ?: emptyList()
    }
    LazyColumn(
        modifier
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
                    misskeyInstanceInfosResponse = null
                ),
                onShowPrivacyPolicy = {},
                onShowTermsOfService = {},
                onTogglePrivacyPolicyAgreement = {},
                onToggleTermsOfServiceAgreement = {},
                onToggleAcceptMastodonAlphaTest = {}

            )
        }
    }
}