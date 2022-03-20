package jp.panta.misskeyandroidclient.model.channel

import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant

data class Channel(
    val id: Id,
    val createdAt: Instant,
    val lastNotedAt: Instant?,
    val name: String,
    val description: String?,
    val bannerUrl: String?,
    val notesCount: Int,
    val usersCount: Int,
    val userId: User.Id?,
) {
    data class Id(
        val accountId: Long,
        val channelId: String
    )
}