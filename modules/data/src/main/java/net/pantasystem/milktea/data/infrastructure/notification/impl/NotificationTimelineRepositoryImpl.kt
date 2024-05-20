package net.pantasystem.milktea.data.infrastructure.notification.impl

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationEntity
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationTimelineEntity
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationTimelineExcludedTypeEntity
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationTimelineIncludedTypeEntity
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationTimelineItemEntity
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationTimelineRelation
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationTimelineRepository
import javax.inject.Inject

class NotificationTimelineRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val accountRepository: AccountRepository,
    private val notificationAdder: NotificationCacheAdder,
    private val coroutineScope: CoroutineScope,
    private val dataBase: DataBase,
) : NotificationTimelineRepository {

    // 最後に初期読み込みした時間をマップに保存しておく(key = accountId, value = lastFetchTime)
    private var lastFetchTimeMap = mapOf<Long, Long>()
    override suspend fun findPreviousTimeline(
        accountId: Long,
        untilId: String?,
        limit: Int,
        excludeTypes: List<String>?,
        includeTypes: List<String>?,
    ): Result<List<Notification>> = runCancellableCatching {
        val timelineHolder = makeNotificationTimelineHolder(
            accountId = accountId,
            excludeTypes = excludeTypes ?: emptyList(),
            includeTypes = includeTypes ?: emptyList()
        )
        val account = accountRepository.get(accountId).getOrThrow()
        val models = if (untilId == null) {
            dataBase.notificationTimelineDAO().findNotifications(timelineHolder.timeline.id, limit)
        } else {
            dataBase.notificationTimelineDAO().findNotificationsUntilId(timelineHolder.timeline.id, untilId, limit)
        }.map {
            it.toModel()
        }
        val lastInitialFetchTime = lastFetchTimeMap[accountId] ?: System.currentTimeMillis()
        val now = System.currentTimeMillis()
        val fetched = if (models.size < limit) {
            fetch(timelineHolder.timeline.id, account, untilId, excludeTypes, includeTypes).getOrThrow()
        } else {
            coroutineScope.launch {
                if (now - lastInitialFetchTime > 1000 * 60 * 3) {
                    fetch(timelineHolder.timeline.id, account, untilId, excludeTypes, includeTypes)
                }
            }
            models
        }

        if (untilId == null) {
            lastFetchTimeMap += accountId to now
        }

        fetched
    }

    private suspend fun fetch(
        timelineId: Long,
        account: Account,
        untilId: String?,
        excludeTypes: List<String>?,
        includeTypes: List<String>?,
    ): Result<List<Notification>> =
        runCancellableCatching {
            when (account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                    fetchMisskeyNotifications(account, untilId, excludeTypes, includeTypes).map {
                        notificationAdder.addAndConvert(account, it).notification
                    }
                }

                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    fetchMastodonNotifications(account, untilId, excludeTypes, includeTypes).map {
                        notificationAdder.addConvert(account, it).notification
                    }
                }
            }.also { notifications ->
                dataBase.notificationTimelineDAO().insertNotificationItems(
                    notifications.map {
                        NotificationTimelineItemEntity(
                            timelineId = timelineId,
                            notificationId = NotificationEntity.makeId(it.id.accountId, it.id.notificationId),
                            cachedAt = System.currentTimeMillis(),
                        )
                    }
                )
            }
        }

    private suspend fun fetchMisskeyNotifications(
        account: Account,
        untilId: String?,
        excludeTypes: List<String>?,
        includeTypes: List<String>?,
    ): List<NotificationDTO> {
        val res = misskeyAPIProvider.get(account).notification(
            NotificationRequest(
                i = account.token,
                untilId = untilId,
                excludeTypes = excludeTypes,
                includeTypes = includeTypes,
            )
        ).throwIfHasError()
        return res.body() ?: emptyList()
    }

    private suspend fun fetchMastodonNotifications(
        account: Account,
        untilId: String?,
        excludeTypes: List<String>?,
        includeTypes: List<String>?,
    ): List<MstNotificationDTO> {
        val res = mastodonAPIProvider.get(account).getNotifications(
            maxId = untilId,
            excludeTypes = excludeTypes,
            types = includeTypes,
        )
        return res.body() ?: emptyList()
    }

    private suspend fun makeNotificationTimelineHolder(
        accountId: Long,
        includeTypes: List<String>,
        excludeTypes: List<String>,
    ): NotificationTimelineRelation {
        val dao = dataBase.notificationTimelineDAO()
        return dataBase.withTransaction {
            val exists = if (includeTypes.isEmpty() && excludeTypes.isEmpty()) {
                dao.findEmpty(accountId).firstOrNull()
            } else if (includeTypes.isEmpty()) {
                dao.findByExcludeTypes(accountId, excludeTypes).firstOrNull()
            } else if (excludeTypes.isEmpty()) {
                dao.findByIncludeTypes(accountId, includeTypes).firstOrNull()
            }
            else {
                dao.findByExcludeTypesAndIncludeTypes(
                    accountId = accountId,
                    excludeTypes = excludeTypes,
                    includeTypes = includeTypes
                ).firstOrNull()
            }
            if (exists == null) {
                val id = dao.insert(
                    NotificationTimelineEntity(
                        accountId = accountId,
                    )
                )
                dao.insertExcludedTypes(
                    excludeTypes.map {
                        NotificationTimelineExcludedTypeEntity(
                            timelineId = id,
                            type = it
                        )
                    }
                )
                dao.insertIncludedTypes(
                    includeTypes.map {
                        NotificationTimelineIncludedTypeEntity(
                            timelineId = id,
                            type = it
                        )
                    }
                )
                dao.findById(id)!!
            } else {
                exists
            }
        }
    }
}