package net.pantasystem.milktea.api_streaming.mastodon

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

interface StreamingAPI {

    fun connectLocalPublic(): Flow<TootStatusDTO>
    fun connectPublic(): Flow<TootStatusDTO>
    fun connectHashTag(tag: String): Flow<TootStatusDTO>
    fun connectUserList(listId: String): Flow<TootStatusDTO>
    fun connectUser(): Flow<Event>
}