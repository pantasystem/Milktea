package net.pantasystem.milktea.data.infrastructure.streaming

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import net.pantasystem.milktea.api_streaming.Socket


fun Socket.stateEvent() : Flow<Socket.State>{
    return channelFlow {
        val listener: (Socket.State)->Unit = { state: Socket.State ->
            trySend(state)
        }
        addStateEventListener(listener)
        awaitClose {
            removeStateEventListener(listener)
        }
    }
}