package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.api_streaming.mastodon.Event
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationJsonCacheRecord
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationJsonCacheRecordDAO
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
    private val loggerFactory: Logger.Factory,
    private val notificationJsonCacheRecordDAO: NotificationJsonCacheRecordDAO,
    @IODispatcher val ioDispatcher: CoroutineDispatcher,
) : NotificationStreaming {

    val logger by lazy {
        loggerFactory.create("NotificationStreamingImpl")
    }

    private val decoder = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun connect(getAccount: () -> Account): Flow<NotificationRelation> {
        return getAccount.asFlow().flatMapLatest { account ->
            when (account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    requireNotNull(channelAPIWithAccountProvider.get(account)).connect(ChannelAPI.Type.Main)
                        .mapNotNull { body ->
                            body as? ChannelBody.Main.Notification
                        }.map {
                        runCancellableCatching {
                            appendCache(account, it)
                        }.onFailure {
                            logger.error("キャッシュへの追加に失敗", it)
                        }
                        notificationCacheAdder.addAndConvert(account, it.body)
                    }
                }
                Account.InstanceType.MASTODON -> requireNotNull(streamingAPIProvider.get(account)).connectUser()
                    .mapNotNull {
                        (it as? Event.Notification)?.notification
                    }.mapNotNull {
                    runCancellableCatching {
                        appendCache(account, it)
                    }.onFailure {
                        logger.error("キャッシュへの追加に失敗", it)
                    }
                    runCatching {
                        notificationCacheAdder.addConvert(account, it)
                    }.getOrNull()
                }
            }
        }.catch {
            logger.error("streaming listen error", it)
        }
    }

    private suspend fun appendCache(account: Account, event: ChannelBody.Main.Notification) {
        withContext(ioDispatcher) {
            val item: NotificationItem = NotificationItem.Misskey(
                notificationDTO = event.body,
                accountId = account.accountId,
                id = event.id,
                nextId = null
            )
            val record = NotificationJsonCacheRecord(
                item.accountId,
                json = decoder.encodeToString(item),
                key = null,
                notificationId = item.id,
                weight = 0
            )
            val items =
                listOf(record) + notificationJsonCacheRecordDAO.findByNullKey(item.accountId)
            notificationJsonCacheRecordDAO.insertAll(items.mapIndexed { index, notificationJsonCacheRecord ->
                notificationJsonCacheRecord.copy(weight = index)
            })
        }
    }

    private suspend fun appendCache(account: Account, event: MstNotificationDTO) {
        withContext(ioDispatcher) {
            val item: NotificationItem = NotificationItem.Mastodon(
                mstNotificationDTO = event,
                accountId = account.accountId,
                id = event.id,
                nextId = null
            )
            val record = NotificationJsonCacheRecord(
                item.accountId,
                json = decoder.encodeToString(item),
                key = null,
                notificationId = item.id,
                weight = 0
            )
            val items =
                listOf(record) + notificationJsonCacheRecordDAO.findByNullKey(item.accountId)
            notificationJsonCacheRecordDAO.insertAll(items.mapIndexed { index, notificationJsonCacheRecord ->
                notificationJsonCacheRecord.copy(weight = index)
            })
        }
    }
}