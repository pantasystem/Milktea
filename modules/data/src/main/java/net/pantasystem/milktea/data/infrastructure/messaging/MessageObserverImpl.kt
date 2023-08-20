package net.pantasystem.milktea.data.infrastructure.messaging

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageObserver
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MessageObserverImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    private val messageAdder: MessageAdder,
) : MessageObserver {


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeAllAccountsMessages(): Flow<Message>{
        return suspend {
            accountRepository.findAll().getOrThrow()
        }.asFlow().flatMapLatest {
            it.map{ ac ->
                observeAccountMessages(ac)
            }.merge()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeByMessagingId(messagingId: MessagingId): Flow<Message>{
        return flow {
            val accountId = when(messagingId) {
                is MessagingId.Direct -> messagingId.userId.accountId
                is MessagingId.Group -> messagingId.groupId.accountId
            }
            emit(accountRepository.get(accountId).getOrNull())
        }.filterNotNull().filter {
            it.instanceType == Account.InstanceType.MISSKEY || it.instanceType == Account.InstanceType.FIREFISH
        }.flatMapLatest { ac ->
            requireNotNull(channelAPIProvider.get(ac)).connect(ChannelAPI.Type.Main).map{
                (it as? ChannelBody.Main.MessagingMessage)?.body
            }.filterNotNull().map {
                messageAdder.add(ac, it)
            }.map {
                it.message
            }.filter {
                messagingId == it.messagingId(ac)
            }
        }
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeAccountMessages(ac: Account): Flow<Message>{
        return suspend {
            channelAPIProvider.get(ac)
        }.asFlow().filterNotNull().flatMapLatest {
            it.connect(ChannelAPI.Type.Main)
        }.map{
            (it as? ChannelBody.Main.MessagingMessage)?.body
        }.filterNotNull().map {
            messageAdder.add(ac, it)
        }.map {
            it.message
        }
    }



}