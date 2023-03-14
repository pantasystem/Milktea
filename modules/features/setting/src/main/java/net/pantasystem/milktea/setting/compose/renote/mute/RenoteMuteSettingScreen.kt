package net.pantasystem.milktea.setting.compose.renote.mute

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.viewmodel.RenoteMuteSettingUiState

@Composable
fun RenoteMuteSettingScreen(
    uiState: RenoteMuteSettingUiState,
    onRemoveRenoteMuteButtonClicked: (RenoteMute) -> Unit,
    onUserClicked: (User) -> Unit,
    onRefresh: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings_renote_mute_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "navigate up")
                    }
                }
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = uiState.syncState is ResultState.Loading),
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.renoteMutes.size) {
                    val item = uiState.renoteMutes[it]
                    RemovableSimpleUserCard(
                        user = item.user,
                        accountHost = uiState.currentAccount?.getHost(),
                        onClick = {
                            when (val user = item.user) {
                                null -> Unit
                                else -> {
                                    onUserClicked(user)
                                }
                            }
                        },
                        onDeleteButtonClicked = {
                            onRemoveRenoteMuteButtonClicked(item.renoteMute)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RemovableSimpleUserCard(
    user: User?,
    accountHost: String?,
    onClick: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
) {

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user?.avatarUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    CustomEmojiText(
                        text = user?.displayName ?: "",
                        emojis = user?.emojis ?: emptyList(),
                        sourceHost = user?.host,
                        accountHost = accountHost,
                        parsedResult = user?.parsedResult,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = user?.displayUserName ?: "")
                }
            }
            IconButton(onClick = onDeleteButtonClicked) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }

    }
}
