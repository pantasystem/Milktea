package net.pantasystem.milktea.model.channel

import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.getRGB
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.user.User

/**
 * @param isFollowing followしているとtrue,未認証の場合はnullになる
 * @param hasUnreadNote 未読ノートが存在する場合trueになる,未認証の場合はnullになる
 */
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
    val isFollowing: Boolean?,
    val hasUnreadNote: Boolean?,
) {
    data class Id(
        val accountId: Long,
        val channelId: String
    ) : EntityId


    val rgpFromName: Triple<Int, Int, Int> by lazy {
        name.getRGB()
    }

}