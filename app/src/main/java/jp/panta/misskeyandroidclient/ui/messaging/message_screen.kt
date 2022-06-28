package jp.panta.misskeyandroidclient.ui.messaging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd

@Composable
fun MessageScreen(
    messageViewModel: MessageViewModel,
) {
    val messages by messageViewModel.messages.collectAsState()

    val scrollState = rememberLazyListState()

    LaunchedEffect(key1 = scrollState) {
        snapshotFlow {
            scrollState.isScrolledToTheEnd()
        }.distinctUntilChanged().collect {
            if (it) {
                messageViewModel.loadOld()
            }
        }
    }

    Scaffold {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            when (val content = messages.content) {
                is StateContent.Exist -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        reverseLayout = true,
                        state = scrollState
                    ) {
                        items(content.rawContent.size) { index ->
                            val message = content.rawContent[index]

                            Box(Modifier.padding(4.dp)) {
                                if (message.isMine()) {
                                    SelfMessageBubble(message = message.message)
                                } else {
                                    RecipientMessageBubble(
                                        user = message.user,
                                        message = message.message
                                    )
                                }
                            }
                        }
                    }
                }
                is StateContent.NotExist -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(id = R.string.input_message))
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Cloud, contentDescription = "Pick a File")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }

            }
        }
    }
}
