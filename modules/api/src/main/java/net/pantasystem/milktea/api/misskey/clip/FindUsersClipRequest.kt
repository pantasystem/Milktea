package net.pantasystem.milktea.api.misskey.clip

data class FindUsersClipRequest(
    val i: String,
    val userId: String,
    val limit: Int,
    val untilId: String? = null,
    val sinceId: String? = null,
)