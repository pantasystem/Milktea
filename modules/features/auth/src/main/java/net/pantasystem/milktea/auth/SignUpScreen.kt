package net.pantasystem.milktea.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.api.misskey.infos.InstanceInfosResponse
import net.pantasystem.milktea.auth.viewmodel.SignUpUiState

@Composable
fun SignUpScreen(
    instanceDomain: String,
    uiState: SignUpUiState,
    onInputKeyword: (String) -> Unit,
    onNextButtonClicked: () -> Unit,
    onSelected: (InstanceInfosResponse.InstanceInfo) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("インスタンスを探す")
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                Text("登録画面がWebブラウザで表示されます")
                Button(
                    shape = RoundedCornerShape(32.dp),
                    onClick = onNextButtonClicked
                ) {
                    Text(
                        "次へ",
                        modifier = Modifier.padding(horizontal = 64.dp)
                    )
                }
            }
        }
    }
}