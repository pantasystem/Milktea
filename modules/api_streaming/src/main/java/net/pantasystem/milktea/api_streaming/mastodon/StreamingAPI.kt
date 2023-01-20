package net.pantasystem.milktea.api_streaming.mastodon

import kotlinx.coroutines.flow.Flow

interface StreamingAPI {

    fun connect(to: ConnectTo): Flow<Event>
}