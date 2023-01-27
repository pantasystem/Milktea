package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.api_streaming.mastodon.Event
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.streaming.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.data.streaming.StreamingAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.notification.NotificationStreaming
import javax.inject.Inject

class NotificationStreamingImpl @Inject constructor(
    val channelAPIWithAccountProvider: ChannelAPIWithAccountProvider,
    private val notificationCacheAdder: NotificationCacheAdder,
    private val streamingAPIProvider: StreamingAPIProvider,
    private val loggerFactory: Logger.Factory
): NotificationStreaming {

    val logger by lazy {
        loggerFactory.create("NotificationStreamingImpl")
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun connect(getAccount: () -> Account): Flow<NotificationRelation> {
        return getAccount.asFlow().flatMapLatest {  account ->
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    requireNotNull(channelAPIWithAccountProvider.get(account)).connect(ChannelAPI.Type.Main).mapNotNull { body ->
                        body as? ChannelBody.Main.Notification
                    }.map {
                        notificationCacheAdder.addAndConvert(account, it.body)
                    }
                }
                Account.InstanceType.MASTODON -> requireNotNull(streamingAPIProvider.get(account)).connectUser().mapNotNull {
                    (it as? Event.Notification)?.notification
                }.mapNotNull{
                    runCatching {
                        notificationCacheAdder.addConvert(account, it)
                    }.getOrNull()
                }
            }
        }.catch {
            logger.error("streaming listen error", it)
        }
    }
}