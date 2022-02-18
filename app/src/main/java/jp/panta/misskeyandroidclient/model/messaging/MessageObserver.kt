package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageObserver @Inject constructor(
    private val accountRepository: AccountRepository,
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    private val getters: Getters
){


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observeAllAccountsMessages(): Flow<Message>{
        return suspend {
            accountRepository.findAll()
        }.asFlow().flatMapMerge {
            it.map{ ac ->
                observeAccountMessages(ac)
            }.merge()
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observeByMessagingId(messagingId: MessagingId): Flow<Message>{
        return flow {
            val accountId = when(messagingId) {
                is MessagingId.Direct -> messagingId.userId.accountId
                is MessagingId.Group -> messagingId.groupId.accountId
            }
            emit(accountRepository.get(accountId))
        }.flatMapLatest { ac ->
            channelAPIProvider.get(ac).connect(ChannelAPI.Type.Main).map{
                (it as? ChannelBody.Main.MessagingMessage)?.body
            }.filterNotNull().map {
                getters.messageRelationGetter.get(ac, it)
            }.map {
                it.message
            }.filter {
                messagingId == it.messagingId(ac)
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observeAccountMessages(ac: Account): Flow<Message>{
        return suspend {
            channelAPIProvider.get(ac)
        }.asFlow().flatMapLatest {
            it.connect(ChannelAPI.Type.Main)
        }.map{
            (it as? ChannelBody.Main.MessagingMessage)?.body
        }.filterNotNull().map {
            getters.messageRelationGetter.get(ac, it)
        }.map {
            it.message
        }
    }



}