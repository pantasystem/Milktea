package net.pantasystem.milktea.data.model.streaming

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.data.streaming.ChannelBody
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.channel.ChannelAPIWithAccountProvider
import javax.inject.Inject
import javax.inject.Singleton

class MediatorMainEventDispatcher(val logger: Logger) {

    @Singleton
    class Factory @Inject constructor(
        val loggerFactory: Logger.Factory,
        val messageDataSource: MessageDataSource,
        val getters: Getters,
        val unreadNotificationDAO: UnreadNotificationDAO,
        val userDataSource: UserDataSource,
    ) {

        fun create(): MediatorMainEventDispatcher {
            return MediatorMainEventDispatcher(loggerFactory.create("MediatorMainEventDispatcher"))
                .attach(
                    StreamingMainMessageEventDispatcher(
                        messageDataSource,
                        getters.messageRelationGetter
                    )
                )
                .attach(
                    StreamingMainNotificationEventDispatcher(
                        getters.notificationRelationGetter,
                        unreadNotificationDAO
                    )
                )
                .attach(StreamingMainUserEventDispatcher(userDataSource))
        }
    }

    private var dispatchers = mutableSetOf<StreamingMainEventDispatcher>()

    fun attach(dispatcher: StreamingMainEventDispatcher): MediatorMainEventDispatcher {
        synchronized(dispatchers) {
            dispatchers = dispatchers.toMutableSet().also {
                it.add(dispatcher)
            }
        }
        return this
    }


    suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main) {
        val iterator = dispatchers.iterator()
        while (iterator.hasNext()) {
            val result = runCatching {
                iterator.next().dispatch(account, mainEvent)
            }.getOrElse {
                false
            }
            if (result) {
                return
            }
        }
    }


}

@Singleton
class ChannelAPIMainEventDispatcherAdapter @Inject constructor(
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    private val accountStore: AccountStore,
    private val applicationScope: CoroutineScope,
    loggerFactory: Logger.Factory
) {
    val logger = loggerFactory.create("MainEventDispatcher")

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(mainDispatcher: MediatorMainEventDispatcher) {
        accountStore.state.map { it.currentAccount }.filterNotNull().flatMapLatest { ac ->
            channelAPIProvider.get(ac).connect(ChannelAPI.Type.Main).map { body ->
                ac to body
            }
        }.mapNotNull {
            (it.second as? ChannelBody.Main)?.let { main ->
                it.first to main
            }
        }.onEach {
            mainDispatcher.dispatch(it.first, it.second)
        }.catch { e ->
            logger.error("Dispatch時にエラー発生", e = e)
        }.launchIn(applicationScope + Dispatchers.IO)
    }
}