package jp.panta.misskeyandroidclient.ui.messaging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel
import net.pantasystem.milktea.common.StateContent

@Composable
fun MessageScreen(
    messageViewModel: MessageViewModel,
) {
    val messages by messageViewModel.messages.collectAsState()

    Scaffold {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()) {
            when (val content = messages.content) {
                is StateContent.Exist -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = true) {
                        items(content.rawContent.size) { index ->
                            val message = content.rawContent[index]

                            Box(Modifier.padding(4.dp)) {
                                if (message.isMine()) {
                                    SelfMessageBubble(message = message.message)
                                } else {
                                    RecipientMessageBubble(user = message.user, message = message.message)
                                }
                            }
                        }
                    }
                }
                is StateContent.NotExist -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}