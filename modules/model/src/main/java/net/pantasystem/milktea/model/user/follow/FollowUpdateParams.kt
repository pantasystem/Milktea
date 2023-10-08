package net.pantasystem.milktea.model.user.follow


/**
 * @param isNotify misskey, mastodonにて通知を受け取るかどうか
 * @param isReblog mastodonにてホームタイムラインでそのユーザのリブログを表示するかどうか
 * @param withReplies misskeyにてそのユーザのリプライを表示するかどうか
 */
data class FollowUpdateParams(
    val isNotify: Boolean? = null,
    val isReblog: Boolean? = null,
    val withReplies: Boolean? = null,
)