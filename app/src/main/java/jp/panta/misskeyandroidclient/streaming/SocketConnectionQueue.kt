package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SocketConnectionQueue(
    private val socketWithAccountProvider: SocketWithAccountProvider,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) {
    private val logger = loggerFactory.create("SocketConnectionQueue")
    private val scope = coroutineScope + dispatcher
    private val connectQueueMap = mutableMapOf<Socket, MutableSharedFlow<Unit>>()

    fun connect(account: Account) {
        val socket = socketWithAccountProvider.get(account)
        connect(socket)
    }

    fun connect(socket: Socket) {
        val queue: MutableSharedFlow<Unit>
        = synchronized(connectQueueMap) {
            var queue = connectQueueMap[socket]
            if(queue != null){
                return@synchronized queue
            }
            queue = MutableSharedFlow(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            connectQueueMap[socket] = queue
            queue.onEach {
                logger.debug("接続を試みた")
                socket.blockingConnect().also {
                    logger.debug("接続要求可否 : ${if(it) "ok" else "failure"}")
                }
            }.launchIn(scope)
            queue
        }
        logger.debug(if(queue.tryEmit(Unit)) "QueueへのPushを失敗" else "queueへのPush成功")
    }
}