package net.pantasystem.milktea.api_streaming.mastodon

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

class StreamingAPIImpl : StreamingAPI {
    override fun connectLocalPublic(): Flow<TootStatusDTO> {
        return flow {  }
    }

    override fun connectPublic(): Flow<TootStatusDTO> {
        return flow {  }

    }

    override fun connectHashTag(tag: String): Flow<TootStatusDTO> {
        return flow {  }

    }

    override fun connectUserList(listId: String): Flow<TootStatusDTO> {
        return flow {  }

    }

    override fun connectUser(): Flow<Event> {
        return flow {  }
    }

}