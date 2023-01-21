package net.pantasystem.milktea.api_streaming.mastodon

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException

class StreamingAPIImpl(
    val host: String,
    val token: String,
    val okHttpClient: OkHttpClient,
) : StreamingAPI {



    private sealed interface ConnectType {
        object LocalPublic : ConnectType
        object Public : ConnectType
        data class Hashtag(val tag: String) : ConnectType
        data class UserList(val listId: String): ConnectType
        object User : ConnectType
    }

    private fun interface StreamingEventHandler {
        operator fun invoke(e: Event)
    }

    val decoder = Json {
        ignoreUnknownKeys = true
    }

    private val listenersMap = mutableMapOf<ConnectType, Set<Listener>>()

    private val connections = mutableMapOf<ConnectType, EventSource>()


    override fun connectLocalPublic(): Flow<TootStatusDTO> {
        return channelFlow {
            val listener = connect(ConnectType.LocalPublic) {
                if (it is Event.Update) {
                    trySend(it.status)
                }
            }
            awaitClose {
                listener.close()
            }
        }
    }

    override fun connectPublic(): Flow<TootStatusDTO> {
        return channelFlow {
            val listener = connect(ConnectType.Public) {
                if (it is Event.Update) {
                    trySend(it.status)
                }
            }
            awaitClose {
                listener.close()
            }
        }

    }

    override fun connectHashTag(tag: String): Flow<TootStatusDTO> {
        return channelFlow {
            val listener = connect(ConnectType.Hashtag(tag)) {
                if (it is Event.Update) {
                    trySend(it.status)
                }
            }
            awaitClose {
                listener.close()
            }
        }

    }

    override fun connectUserList(listId: String): Flow<TootStatusDTO> {
        return channelFlow {
            val listener = connect(ConnectType.UserList(listId)) {
                if (it is Event.Update) {
                    trySend(it.status)
                }
            }
            awaitClose {
                listener.close()
            }
        }

    }

    override fun connectUser(): Flow<Event> {
        return channelFlow {
            val listener = connect(ConnectType.User) {
                trySend(it)
            }
            awaitClose {
                listener.close()
            }
        }
    }


    private fun connect(connectType: ConnectType, handler: StreamingEventHandler): Listener {
        val listener = Listener(connectType, handler)
        synchronized(listenersMap) {
            val listeners = listenersMap[connectType] ?: mutableSetOf()
            listenersMap[connectType] = listeners + listener
        }

        val call = synchronized(listenersMap) {
            if (connections[connectType] == null) {
                val request = EventSources.createFactory(okHttpClient).newEventSource(
                    Request.Builder().url(
                        "https://$host/api/v1/streaming/public/local"
                    ).build(),
                    SseEventHandler(ConnectType.LocalPublic)
                ).request()
                okHttpClient.newCall(request)
            } else {
                null
            }
        }

        call?.enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) = Unit

            override fun onResponse(call: Call, response: Response) = Unit
        })
        return listener

    }

    private inner class SseEventHandler(val connectType: ConnectType) : EventSourceListener() {
        override fun onClosed(eventSource: EventSource) {
            super.onClosed(eventSource)

            synchronized(listenersMap) {
                listenersMap.remove(connectType)
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            super.onFailure(eventSource, t, response)

            synchronized(listenersMap) {
                connections.remove(connectType)
            }

        }

        override fun onOpen(eventSource: EventSource, response: Response) {
            super.onOpen(eventSource, response)
            synchronized(listenersMap) {
                if (connections[connectType] != null) {
                    connections[connectType]?.cancel()
                }
                connections[connectType] = eventSource
            }
        }

        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            super.onEvent(eventSource, id, type, data)

            val listeners = synchronized(listenersMap) {
                listenersMap[connectType]
            }

            listeners ?: return

            val event = when(type) {
                "update" -> {
                    Event.Update(decoder.decodeFromString<TootStatusDTO>(data))
                }
                "notification" -> {
                    Event.Notification(data)
                }
                "delete" -> {
                    Event.Delete(data)
                }
                else -> {
                    return
                }
            }

            listeners.map {
                it.handler(event)
            }

        }
    }

    private inner class Listener(
        val connectType: ConnectType,
        val handler: StreamingEventHandler,
    ) {
        fun close() {
            synchronized(listenersMap) {
                listenersMap[connectType]?.toMutableSet()?.also {
                    it.remove(this)
                    listenersMap[connectType] = it
                }
                if (listenersMap[connectType].isNullOrEmpty()) {
                    connections.remove(connectType)?.cancel()
                }
            }
        }
    }


}