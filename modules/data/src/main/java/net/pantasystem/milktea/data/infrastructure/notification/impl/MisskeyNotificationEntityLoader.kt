package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.IdPreviousLoader
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account

class MisskeyNotificationEntityLoader(
    val account: Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
) : IdPreviousLoader<String, NotificationItem, NotificationAndNextId> {
    override suspend fun loadPrevious(state: PageableState<List<NotificationAndNextId>>, id: String?): Result<List<NotificationItem>> = runCancellableCatching {
        val res = misskeyAPIProvider.get(account).notification(
            NotificationRequest(
                i = account.token,
                untilId = id,
            )
        )
        requireNotNull(res.body()).map {
            NotificationItem.Misskey(
                it,
                it.id,
                it.id,
                account.accountId
            )
        }
    }
}
