package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.model.Entity
import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.Note
import java.io.Serializable
import java.lang.Exception
import java.util.*

/**
 * Userはfollowやunfollowなどは担当しない
 * Userはfollowやunfollowに関連しないため
 */
sealed class User : Entity{

    abstract val id: Id
    abstract val userName: String
    abstract val name: String?
    abstract val avatarUrl: String?
    abstract val emojis: List<Emoji>
    abstract val isCat: Boolean?
    abstract val isBot: Boolean?
    abstract val host: String?
    abstract var instanceUpdatedAt: Date

    data class Id(
        val accountId: Long,
        val id: String,
    ) : EntityId

    data class Simple(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<Emoji>,
        override val isCat: Boolean?,
        override val isBot: Boolean?,
        override val host: String?,
        override var instanceUpdatedAt: Date = Date()
    ) : User()

    data class Detail(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<Emoji>,
        override val isCat: Boolean?,
        override val isBot: Boolean?,
        override val host: String?,
        val description: String?,
        val followersCount: Int?,
        val followingCount: Int?,
        val hostLower: String?,
        val notesCount: Int?,
        val pinnedNoteIds: List<Note.Id>?,
        val bannerUrl: String?,
        val url: String?,
        val isFollowing: Boolean,
        val isFollower: Boolean,
        val isBlocking: Boolean,
        val isMuting: Boolean,
        val hasPendingFollowRequestFromYou: Boolean,
        val hasPendingFollowRequestToYou: Boolean,
        val isLocked: Boolean,
        override var instanceUpdatedAt: Date = Date()
    ) : User() {
        val followState: FollowState
            get() {
                if(isFollowing) {
                    return FollowState.FOLLOWING
                }
                
                if(isLocked) {
                    return if(hasPendingFollowRequestFromYou) {
                        FollowState.PENDING_FOLLOW_REQUEST
                    }else{
                        FollowState.UNFOLLOWING_LOCKED
                    }
                }

                return FollowState.UNFOLLOWING
            }
    }

    fun updated(){
        instanceUpdatedAt = Date()
    }

    fun getDisplayUserName(): String{
        return "@" + this.userName + if(this.host == null){
            ""
        }else{
            "@" + this.host
        }
    }

    fun getDisplayName(): String{
        return name?: userName
    }

    fun getShortDisplayName(): String{
        return "@" + this.userName
    }

    fun getProfileUrl(): String {
        return "https://$host/${getDisplayUserName()}"
    }
}

enum class FollowState {
    PENDING_FOLLOW_REQUEST, FOLLOWING, UNFOLLOWING, UNFOLLOWING_LOCKED
}

sealed class UserState {
    data class Removed(val id: User.Id) : UserState()
    data class Error(val exception: Exception) : UserState()
    object Loading : UserState()
}