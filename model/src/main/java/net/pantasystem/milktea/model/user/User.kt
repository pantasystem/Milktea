package net.pantasystem.milktea.model.user

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.nickname.UserNickname
import java.lang.Exception
import java.util.*

/**
 * Userはfollowやunfollowなどは担当しない
 * Userはfollowやunfollowに関連しないため
 */
sealed interface User : Entity {

    val id: Id
    val userName: String
    val name: String?
    val avatarUrl: String?
    val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>
    val isCat: Boolean?
    val isBot: Boolean?
    val host: String?
    val nickname: UserNickname?
    var instanceUpdatedAt: Date

    data class Id(
        val accountId: Long,
        val id: String,
    ) : EntityId

    data class Simple(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>,
        override val isCat: Boolean?,
        override val isBot: Boolean?,
        override val host: String?,
        override val nickname: UserNickname?,
        override var instanceUpdatedAt: Date = Date()
    ) : User

    data class Detail(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>,
        override val isCat: Boolean?,
        override val isBot: Boolean?,
        override val host: String?,
        override val nickname: UserNickname?,
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
    ) : User {
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
        return nickname?.name?: name?: userName
    }

    fun getShortDisplayName(): String{
        return "@" + this.userName
    }

    fun getProfileUrl(account: net.pantasystem.milktea.model.account.Account): String {
        return "https://${account.getHost()}/${getDisplayUserName()}"
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