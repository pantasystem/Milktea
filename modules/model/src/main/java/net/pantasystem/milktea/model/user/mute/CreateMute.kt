package net.pantasystem.milktea.model.user.mute

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

/**
 * @param userId ミュートする対象のUserのId
 * @param expiresAt ミュートの期限
 * @param notifications Mastodonのためのパラメータで、ミュートしたユーザからの通知の有無を指定できる
 */
data class CreateMute(
    val userId: User.Id,
    val expiresAt: Instant? = null,
    val notifications: Boolean? = null
)
