package jp.panta.misskeyandroidclient.model.channel

import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.Hash
import jp.panta.misskeyandroidclient.util.getRGB
import kotlinx.datetime.Instant

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
    )


    val rgpFromName: Triple<Int, Int, Int> by lazy {
        name.getRGB()
    }

}