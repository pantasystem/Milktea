package jp.panta.misskeyandroidclient.model.streaming

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.viewmodel.MiCore

class MediatorMainEventDispatcher(val logger: Logger) {

    class Factory(private val miCore: MiCore) {

        fun create(): MediatorMainEventDispatcher {
            return MediatorMainEventDispatcher(miCore.loggerFactory.create("MediatorMainEventDispatcher"))
                .attach(StreamingMainMessageEventDispatcher(miCore.getMessageDataSource(), miCore.getGetters().messageRelationGetter))
                .attach(StreamingMainNotificationEventDispatcher(miCore.getGetters().notificationRelationGetter, miCore.getUnreadNotificationDAO()))
                .attach(StreamingMainUserEventDispatcher(miCore.getUserDataSource()))
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

    fun detach(dispatcher: StreamingMainEventDispatcher): MediatorMainEventDispatcher {
        synchronized(dispatchers) {
            dispatchers = dispatchers.toMutableSet().also {
                it.remove(dispatcher)
            }
        }
        return this
    }

    suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main) {
        val iterator = dispatchers.iterator()
        while(iterator.hasNext()) {
            val result = runCatching {
                iterator.next().dispatch(account, mainEvent)
            }.getOrElse {
                false
            }
            if(result) {
                return
            }
        }
    }


}