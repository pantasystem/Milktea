package jp.panta.misskeyandroidclient.model.hashtag

data class HashTag(
    val tag: String,
    val mentionedUserCount: Int?,
    val mentionedLocalUserCount: Int?,
    val mentionedRemoteUserCount: Int?,
    val attachedUsersCount: Int?,
    val attachedLocalUsersCount: Int?,
    val attachedRemoteUsersCount: Int?
)