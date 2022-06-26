package jp.panta.misskeyandroidclient.ui.messaging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel

@Composable
fun MessageScreen(
    messageViewModel: MessageViewModel,
) {

    val messages by messageViewModel.messages.collectAsState()


    Scaffold {
        Column(Modifier.padding(it)) {

        }
    }
}