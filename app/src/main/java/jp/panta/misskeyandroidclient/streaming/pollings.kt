package jp.panta.misskeyandroidclient.streaming

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class Pong(val pong: Long)

/**
 * {"type": "api", "body": {"id": "3", "endpoint": "notes/create", "data":{"text":"test", "visibility":"public"}}}
 */
@Serializable
data class PingRequest(
    val type: String,
    val body: Body
)


@Serializable
data class Body(
    val id: String,
    val endpoint: String,
)


fun createPingRequest(): PingRequest{
    return PingRequest(
        "api",
        Body(
            UUID.randomUUID().toString().substring(0..4),
            endpoint = "ping"
        )
    )
}

@Serializable
data class PongRes(
    val type: String,
    val body: Body
) {
    @Serializable
    data class Body(val res: Pong)
    val id: String get() = type.split(":")[1]
}



internal class PollingJob(
    val socket: Socket,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + dispatcher)
    val isPolling: Boolean get() = job.isActive
    fun ping(interval: Long, count: Long) {
        scope.launch {
            (0 until count).asSequence().asFlow().onEach {
                delay(interval)
            }.collect {
                socket.send(json.encodeToString(createPingRequest()))
            }
        }
    }

    fun cancel() {
        job.cancel()
    }
}
