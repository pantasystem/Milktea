package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.model.emoji.Emoji
import java.util.*

/**
 * Userはfollowやunfollowなどは担当しない
 * Userはfollowやunfollowに関連しないため
 */
data class User(
    val id: Id,
    val userName: String,
    val name: String?,
    val avatarUrl: String?,
    val emojis: List<Emoji>,
    val isCat: Boolean?,
    val isBot: Boolean?,
    val state: State? = null,
    val profile: Profile? = null,
    var instanceUpdatedAt: Date = Date()
){

    data class Id(
        val accountId: Long,
        val id: String,
    )
    val isDetail: Boolean
        get() = this.profile != null


    fun updated(){
        instanceUpdatedAt = Date()
    }

    data class State(
        val isFollowing: Boolean,
        val isFollower: Boolean,
        val isBlocking: Boolean,
        val isMuting: Boolean
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (userName != other.userName) return false
        if (name != other.name) return false
        if (avatarUrl != other.avatarUrl) return false
        if (emojis != other.emojis) return false
        if (isCat != other.isCat) return false
        if (isBot != other.isBot) return false
        if (profile != other.profile) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + emojis.hashCode()
        result = 31 * result + isCat.hashCode()
        result = 31 * result + isBot.hashCode()
        result = 31 * result + (profile?.hashCode() ?: 0)
        return result
    }
}