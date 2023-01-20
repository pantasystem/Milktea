package net.pantasystem.milktea.api_streaming.mastodon

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StreamingAPIImpl : StreamingAPI {
    override fun connect(to: ConnectTo): Flow<Event> {
        return flow {  }
    }
}