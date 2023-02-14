package net.pantasystem.milktea.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.api.misskey.infos.InstanceInfosResponse
import net.pantasystem.milktea.auth.viewmodel.SignUpUiState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.instance.InstanceInfoType

@Composable
fun SignUpScreen(
    instanceDomain: String,
    uiState: SignUpUiState,
    onInputKeyword: (String) -> Unit,
    onNextButtonClicked: (InstanceInfoType) -> Unit,
    onSelected: (InstanceInfosResponse.InstanceInfo) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.auth_find_instance))
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "navigate up")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                instanceDomain,
                onValueChange = onInputKeyword,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                maxLines = 1,
                label = {
                    Text(stringResource(R.string.instance_domain))
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onInputKeyword("")
                        },
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "clear instance domain")
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.filteredInfos) { instance ->
                    MisskeyInstanceInfoCard(
                        info = instance,
                        selected = uiState.keyword == instance.url
                                || (uiState.selectedUrl == instance.url && !uiState.filteredInfos.any {
                            it.url == uiState.keyword
                        }),
                        onClick = {
                            onSelected(instance)
                        }
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(id = R.string.auth_find_instance_show_in_web_browser_message))
                Button(
                    shape = RoundedCornerShape(32.dp),
                    onClick = {
                        when(val content = uiState.instanceInfo.content) {
                            is StateContent.Exist -> {
                                onNextButtonClicked(content.rawContent)
                            }
                            is StateContent.NotExist -> Unit
                        }
                    },
                    enabled = uiState.instanceInfo is ResultState.Fixed
                            && uiState.instanceInfo.content is StateContent.Exist
                ) {
                    Text(
                        stringResource(id = R.string.auth_find_instance_next_button_text),
                        modifier = Modifier.padding(horizontal = 64.dp)
                    )
                }
            }
        }
    }
}