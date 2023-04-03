package net.pantasystem.milktea.messaging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd
import net.pantasystem.milktea.messaging.viewmodel.MessageEditorViewModel
import net.pantasystem.milktea.messaging.viewmodel.MessageViewModel
import net.pantasystem.milktea.model.messaging.MessageRelation

@Composable
fun MessageScreen(
    messageViewModel: MessageViewModel,
    messageActionViewModel: MessageEditorViewModel,
    onOpenDriveToSelect: () -> Unit,
    onNavigateUp: () -> Unit
) {

    val messages by messageViewModel.messages.collectAsState()
    val scrollState = rememberLazyListState()
    val account by messageViewModel.account.collectAsState()

    val title by messageViewModel.title.observeAsState()

    LaunchedEffect(key1 = messages) {
        if (messageViewModel.latestReceivedMessageId != null && scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.index == 1) {
            scrollState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(title ?: "")
                }
            )
        },
        modifier = Modifier.windowInsetsPadding(
            WindowInsets
                .navigationBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
    ) {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {

            Messages(
                messageState = messages,
                scrollState = scrollState,
                accountHost = account?.getHost(),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                onLoad = {
                    messageViewModel.loadOld()
                },
            )
            MessageForm(messageActionViewModel = messageActionViewModel, onOpenDriveToSelect)
        }
    }
}


@Composable
@Stable
fun Messages(
    modifier: Modifier = Modifier,
    messageState: PageableState<List<MessageRelation>>,
    accountHost: String?,
    scrollState: LazyListState,
    onLoad: () -> Unit
) {


    LaunchedEffect(key1 = scrollState) {
        snapshotFlow {
            scrollState.isScrolledToTheEnd()
        }.distinctUntilChanged().collect {
            if (it) {
                onLoad.invoke()
            }
        }
    }
    when (val content = messageState.content) {
        is StateContent.Exist -> {
            LazyColumn(
                modifier = modifier,
                reverseLayout = true,
                state = scrollState
            ) {
                items(content.rawContent.size) { index ->
                    val message = content.rawContent[index]

                    Box(Modifier.padding(4.dp)) {
                        if (message.isMine()) {
                            SelfMessageBubble(message = message.message, accountHost = accountHost)
                        } else {
                            RecipientMessageBubble(
                                user = message.user,
                                message = message.message,
                                accountHost = accountHost,
                            )
                        }
                    }
                }
            }
        }
        is StateContent.NotExist -> {
            Box(
                modifier,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

}

@Composable
fun MessageForm(
    messageActionViewModel: MessageEditorViewModel,
    onOpenDriveToSelect: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        val uiState by messageActionViewModel.uiState.collectAsState()
        OutlinedTextField(
            value = uiState.text,
            onValueChange = { text ->
                messageActionViewModel.setText(text)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(stringResource(id = R.string.messaging_input_message))
            }
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDriveToSelect) {
                Icon(Icons.Default.Cloud, contentDescription = "Pick a File")
            }
            if (uiState.file != null) {
                Text(uiState.file?.name ?: "")
            }
            IconButton(onClick = {
                messageActionViewModel.send()
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }

    }
}