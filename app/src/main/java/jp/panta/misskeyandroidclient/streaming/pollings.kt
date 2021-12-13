package jp.panta.misskeyandroidclient.streaming

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    private val pongs = MutableSharedFlow<PongRes>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1024)


    val isPolling: Boolean get() = job.isActive
    fun ping(interval: Long, count: Long, timeout: Long) {
        scope.launch {
            (0 until count).asSequence().asFlow().onEach {
                delay(interval)
            }.collect {
                val ping = createPingRequest()
                if(!socket.send(json.encodeToString(ping))) {
                    Log.d("PollingJob", "sendしたらfalse帰ってきた")
                }
                try {
                    withTimeout(timeout) {
                        pongs.first {
                            it.id == ping.body.id
                        }
                    }
                    Log.d("PollingJob", "polling成功")
                } catch(e: TimeoutCancellationException) {
                    Log.d("PollingJob", "polling失敗")
                    socket.reconnect()
                }
            }
        }
    }


    fun onReceive(res: PongRes) {
        pongs.tryEmit(res)
    }

    fun cancel() {
        job.cancel()
    }
}
