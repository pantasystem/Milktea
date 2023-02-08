package net.pantasystem.milktea.clip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_navigation.ClipListNavigationArgs

@Composable
fun ClipListScreen(
    uiState: ClipListUiState,
    mode: ClipListNavigationArgs.Mode,
    onClipTileClicked: (ClipItemState) -> Unit,
    onToggleAddToTabButtonClicked: (ClipItemState) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "navigate up")
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.clip))
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            when (val content = uiState.clipStatusesState.content) {
                is StateContent.Exist -> {
                    items(content.rawContent) { clipState ->
                        ClipTile(
                            clipState = clipState,
                            isSelectMode = mode != ClipListNavigationArgs.Mode.View,
                            isSelected = mode != ClipListNavigationArgs.Mode.View && clipState.isAddedToTab,
                            onClick = {
                                onClipTileClicked(clipState)
                            },
                            onAddToTabButtonClicked = {
                                onToggleAddToTabButtonClicked(
                                    clipState
                                )
                            }
                        )
                    }
                }
                is StateContent.NotExist -> {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (uiState.clipStatusesState) {
                                is ResultState.Error -> {
                                    Text("Load error")
                                }
                                is ResultState.Fixed -> {
                                    Text("Clip is not exists")
                                }
                                is ResultState.Loading -> {
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