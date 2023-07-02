package net.pantasystem.milktea.api_streaming

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock

const val TTL_COUNT = 3
internal class PollingJob(
    private val socket: Socket,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + dispatcher)
    private val pongs = MutableSharedFlow<String>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1024
    )


    fun startPolling(interval: Long, count: Long, timeout: Long) {

        // NOTE: pingに一定回数以上失敗すると再接続するそのためのカウント
        var ttl = TTL_COUNT
        scope.launch {
            (0 until count).asSequence().asFlow().onEach {
                delay(interval)
            }.collect {
                val sendTime = Clock.System.now()

                if (!socket.send("ping")) {
                    if (BuildConfig.DEBUG) {
                        Log.d("PollingJob", "sendしたらfalse帰ってきた")
                    }
                }
                try {
                    val pong = withTimeout(timeout) {
                        pongs.first {
                            it.isNotBlank()
                        }
                    }
                    val resTime = Clock.System.now()
                    val diff = resTime.toEpochMilliseconds() - sendTime.toEpochMilliseconds()

                    if (BuildConfig.DEBUG) {
                        Log.d("PollingJob", "polling成功 msg:$pong, かかった時間(ミリ秒):${diff}")
                    }
                    // NOTE: pingに成功したらTTLのカウントを初期値に戻す
                    ttl = TTL_COUNT
                } catch (e: TimeoutCancellationException) {
                    if (BuildConfig.DEBUG) {
                        Log.d("PollingJob", "polling失敗")
                    }
                    if (--ttl <= 0) {
                        socket.reconnect()
                    }
                }
            }
        }
    }

    fun onReceive(msg: String) {
        pongs.tryEmit(msg)
    }

    fun cancel() {
        job.cancel()
    }
}
