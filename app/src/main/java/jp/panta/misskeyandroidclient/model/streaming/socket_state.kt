package jp.panta.misskeyandroidclient.model.streaming

import jp.panta.misskeyandroidclient.streaming.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow


@ExperimentalCoroutinesApi
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