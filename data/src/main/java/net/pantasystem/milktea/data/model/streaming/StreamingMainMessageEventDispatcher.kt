package net.pantasystem.milktea.data.model.streaming

import jp.panta.misskeyandroidclient.gettters.MessageRelationGetter
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.streaming.ChannelBody

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