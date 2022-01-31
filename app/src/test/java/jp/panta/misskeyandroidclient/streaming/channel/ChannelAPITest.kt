package jp.panta.misskeyandroidclient.streaming.channel

import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.network.SocketImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChannelAPITest {

    @ExperimentalCoroutinesApi
    @Test
    fun connect(): Unit = runBlocking {
        val wssURL = "wss://misskey.io/streaming"
        val logger = TestLogger.Factory()
        val okHttpClient = OkHttpClient()
        val socket = SocketImpl(wssURL, okHttpClient,logger)
        socket.blockingConnect()

        var count = 0
        launch {
            ChannelAPI(socket, logger).connect(ChannelAPI.Type.Global).collect {
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
        val okHttpClient = OkHttpClient()
        val socket = SocketImpl(wssURL, okHttpClient, logger)
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