package jp.panta.misskeyandroidclient.model.streaming

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountStore
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

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