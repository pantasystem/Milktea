package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.logger.TestLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.api_streaming.network.SocketImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChannelAPITest {

    @ExperimentalCoroutinesApi
    @Test
    fun connect(): Unit = runBlocking {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val socket =
            SocketImpl(wssURL, {false}, logger, DefaultOkHttpClientProvider())
        socket.blockingConnect()

        var count = 0
        launch {
            ChannelAPI(socket, logger)
                .connect(ChannelAPI.Type.Global).collect {
                println(it)
                assertTrue(it is ChannelBody.ReceiveNote)
                count ++
                if(count > 5) {
                    cancel()
                }
            }
        }.join()


    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDisconnect() {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val socket =
            SocketImpl(wssURL, {false}, logger, DefaultOkHttpClientProvider())
        val channelAPI = ChannelAPI(socket, logger)
        runBlocking {

            val job1 = launch {
                channelAPI.connect(ChannelAPI.Type.Main).collect ()
            }

            val job2 = launch {
                channelAPI.connect(ChannelAPI.Type.Global).collect ()
            }

            val job3 = launch {
                channelAPI.connect(ChannelAPI.Type.Global).collect ()
            }

            val closedRes: Socket.State = suspendCoroutine { continuation ->
                var flag = true
                socket.addStateEventListener { ev ->
                    if(flag) {
                        if(ev is Socket.State.Connected) {
                            assertEquals(2, channelAPI.count())
                            job1.cancel()
                            job2.cancel()
                            job3.cancel()
                        }
                        if(ev is Socket.State.Closed) {
                            flag = false

                            continuation.resume(ev)
                        }
                    }
                }
            }
            assertTrue(closedRes is Socket.State.Closed)
        }
    }
}