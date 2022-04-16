package net.pantasystem.milktea.data.streaming

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class Pong(val pong: Long)

@Serializable
data class Error(val message: String)

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


fun createPingRequest(): PingRequest {
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
    data class Body(val res: Pong? = null, val error: Error? = null)


    val id: String get() = type.split(":")[1]
}

const val TTL_COUNT = 3


internal class PollingJob(
    private val socket: Socket,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + dispatcher)
    private val pongs = MutableSharedFlow<PongRes>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1024
    )


    val isPolling: Boolean get() = job.isActive
    fun startPolling(interval: Long, count: Long, timeout: Long) {

        // NOTE: pingに一定回数以上失敗すると再接続するそのためのカウント
        var ttl = TTL_COUNT
        scope.launch {
            (0 until count).asSequence().asFlow().onEach {
                delay(interval)
            }.collect {
                val ping = createPingRequest()
                val sendTime = Clock.System.now()
                if (!socket.send(json.encodeToString(ping))) {
                    Log.d("PollingJob", "sendしたらfalse帰ってきた")
                }
                try {
                    val pong = withTimeout(timeout) {
                        pongs.first {
                            it.id == ping.body.id
                        }
                    }
                    val resTime = Clock.System.now()
                    val diff = resTime.toEpochMilliseconds() - sendTime.toEpochMilliseconds()

                    Log.d("PollingJob", "polling成功 msg:$pong, かかった時間(ミリ秒):${diff}")
                    // NOTE: pingに成功したらTTLのカウントを初期値に戻す
                    ttl = TTL_COUNT
                } catch (e: TimeoutCancellationException) {
                    Log.d("PollingJob", "polling失敗")
                    if (--ttl <= 0) {
                        socket.reconnect()
                    }
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
