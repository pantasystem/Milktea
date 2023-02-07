package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account

class MisskeyNotificationEntityLoader(
    val account: Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val idGetter: IdGetter<String>,
) : PreviousLoader<NotificationItem> {
    override suspend fun loadPrevious(): Result<List<NotificationItem>> = runCancellableCatching {
        val res = misskeyAPIProvider.get(account).notification(
            NotificationRequest(
                i = account.token,
                untilId = idGetter.getUntilId()
            )
        )
        requireNotNull(res.body()).map {
            NotificationItem.Misskey(
                it,
                it.id,
            )
        }
    }
}
