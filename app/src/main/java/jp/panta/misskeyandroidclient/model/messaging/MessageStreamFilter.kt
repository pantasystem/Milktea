package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class MessageStreamFilter(val miCore: MiCore){


    fun getAllMergedAccountMessages(): Flow<Message>{
        return miCore.getAccounts().flatMapMerge {
            it.map{ ac ->
                getAccountMessageObservable(ac)
            }.merge()
        }
    }

    fun getObservable(messagingId: MessagingId): Flow<Message>{
        return flow<Account> {
            val accountId = when(messagingId) {
                is MessagingId.Direct -> messagingId.userId.accountId
                is MessagingId.Group -> messagingId.groupId.accountId
            }
            miCore.getAccountRepository().get(accountId)
        }.flatMapLatest { ac ->
            miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map{
                (it as? ChannelBody.Main.HavingMessagingBody)?.body
            }.filterNotNull().map {
                miCore.getGetters().messageRelationGetter.get(ac, it)
            }.map {
                it.message
            }.filter {
                messagingId == it.messagingId(ac)
            }
        }
    }

    fun getAccountMessageObservable(ac: Account): Flow<Message>{
        return suspend {
            miCore.getChannelAPI(ac)
        }.asFlow().flatMapLatest {
            it.connect(ChannelAPI.Type.MAIN)
        }.map{
            (it as? ChannelBody.Main.HavingMessagingBody)?.body
        }.filterNotNull().map {
            miCore.getGetters().messageRelationGetter.get(ac, it)
        }.map {
            it.message
        }
    }



}