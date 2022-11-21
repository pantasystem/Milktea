package net.pantasystem.milktea.channel

import android.widget.FrameLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import net.pantasystem.milktea.model.channel.Channel

@Composable
fun ChannelDetailScreen(
    onNavigateUp: () -> Unit,
    channelId: Channel.Id,
    channel: Channel?,
    onUpdateFragment: (Int, FrameLayout, Channel.Id) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(channel?.name ?: "")
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier.padding(paddingValues),
            factory = {
            FrameLayout(it).apply {
                id = R.id.container
            }
        }, update = {
            onUpdateFragment(R.id.container, it, channelId)
        })
    }
}