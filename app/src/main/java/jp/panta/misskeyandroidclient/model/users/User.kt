package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.model.emoji.Emoji

/**
 * Userはfollowやunfollowなどは担当しない
 * Userはfollowやunfollowに関連しないため
 */
data class User(
    val id: String,
    val userName: String,
    val name: String,
    val avatarUrl: String,
    val emojis: List<Emoji>,
    val isCat: Boolean,
    val isBot: Boolean
)