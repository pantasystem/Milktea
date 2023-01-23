package net.pantasystem.milktea.data.infrastructure.streaming

import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.data.infrastructure.messaging.MessageAdder
import net.pantasystem.milktea.data.infrastructure.messaging.MessageDataSource
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StreamingMainMessageEventDispatcher @Inject constructor(
    private val messageDataSource: MessageDataSource,
    private val messageAdder: MessageAdder,
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        if(mainEvent is ChannelBody.Main.ReadAllMessagingMessages) {
            messageDataSource.readAllMessages(account.accountId)
        }
        return (mainEvent as? ChannelBody.Main.HavingMessagingBody)?.let{
            messageAdder.add(account, it.body)
        } != null
    }
}