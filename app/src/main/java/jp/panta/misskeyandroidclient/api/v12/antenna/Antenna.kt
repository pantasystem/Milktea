package jp.panta.misskeyandroidclient.api.v12.antenna

import java.io.Serializable

data class Antenna(
    val id: String,
    val name: String,
    val src: String,
    val userListId: String?,
    val userGroupId: String?,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String>,
    val caseSensitive: Boolean,
    val withFile: Boolean,
    val withReplies: Boolean,
    val notify: Boolean,
    val hasUnreadNote: Boolean
) : Serializable