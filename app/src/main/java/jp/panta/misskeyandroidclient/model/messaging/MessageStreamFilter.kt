package jp.panta.misskeyandroidclient.model.messaging

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class MessageStreamFilter(val miCore: MiCore){


    fun getAllMergedAccountMessages(): Flow<Message>{
        return miCore.getAccounts().flatMapMerge {
            it.map{ ac ->
                getAccountMessageObservable(ac)
            }.merge()
        }
    }

    fun getObservable(messagingId: MessagingId, ac: Account): Flow<Message>{
        return miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map{
            (it as? ChannelBody.Main.HavingMessagingBody)?.body
        }.filter {
            it?.messagingId(ac) == messagingId
        }.filterNotNull()
    }

    fun getAccountMessageObservable(ac: Account): Flow<Message>{
        return miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map{
            (it as? ChannelBody.Main.HavingMessagingBody)?.body
        }.filterNotNull()
    }



}