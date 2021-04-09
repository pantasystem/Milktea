package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.filter

class SocketConnectionQueue(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) {
    private val logger = loggerFactory.create("SocketConnectionQueue")
    private val scope = coroutineScope + dispatcher
    //private val connectQueueMap = mutableMapOf<Socket, MutableSharedFlow<Unit>>()
    private val connectQueue = MutableSharedFlow<Socket>(extraBufferCapacity = 1000)

    init {
        connectQueue.filterNot { socket ->
            socket.state() == Socket.State.Connected
        }.onEach { socket ->
            socket.blockingConnect()
            delay(500)
        }.catch { e ->
            logger.error("接続エラー", e = e)
        }.launchIn(scope)
    }

    suspend fun connect(account: Account, force: Boolean = true) {
        val socket = socketWithAccountProvider.get(account)
        connect(socket, force)
    }

    /**
     * @param force trueの場合はリミットを無視して接続します
     */
    suspend fun connect(socket: Socket, force: Boolean = true) {
        if(force) {
            if(socket.state() != Socket.State.Connected) {
                socket.blockingConnect()

            }
            return
        }
        connectQueue.tryEmit(socket)

    }
}