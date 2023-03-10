package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.IdPreviousLoader
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.model.account.Account

class MstNotificationEntityLoader(
    val account: Account,
    private val mastodonAPIProvider: MastodonAPIProvider,
) : IdPreviousLoader<String, NotificationItem, NotificationAndNextId> {
    override suspend fun loadPrevious(state: PageableState<List<NotificationAndNextId>>, id: String?): Result<List<NotificationItem>> = runCancellableCatching {
        val isEmpty = (state.content as? StateContent.Exist)?.rawContent.isNullOrEmpty()
        if (id == null && !isEmpty) {
            return@runCancellableCatching emptyList()
        }
        val res = mastodonAPIProvider.get(account).getNotifications(
            maxId = id
        ).throwIfHasError()

        val body = res.body()

        requireNotNull(body).map {
            NotificationItem.Mastodon(
                it,
                it.id,
                it.id,
                account.accountId
            )
        }
    }
}
