package jp.panta.misskeyandroidclient.model.messaging

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import kotlinx.coroutines.flow.*


class MessageSubscriber(val miCore: MiCore){


    fun getAllMergedAccountMessages(): Flow<Message>{
        val observables = (miCore.getAccounts().value?: emptyList()).map{
            getAccountMessageObservable(it)
        }
        return observables.merge()
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