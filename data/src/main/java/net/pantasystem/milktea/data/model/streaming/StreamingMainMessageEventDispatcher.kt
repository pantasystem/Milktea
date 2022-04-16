package net.pantasystem.milktea.data.model.streaming

import net.pantasystem.milktea.data.gettters.MessageRelationGetter
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.streaming.ChannelBody


class StreamingMainMessageEventDispatcher(
    private val messageDataSource: MessageDataSource,
    private val messagingGetter: MessageRelationGetter
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        if(mainEvent is ChannelBody.Main.ReadAllMessagingMessages) {
            messageDataSource.readAllMessages(account.accountId)
        }
        return (mainEvent as? ChannelBody.Main.HavingMessagingBody)?.let{
            messagingGetter.get(account, it.body)
        } != null
    }
}