package net.pantasystem.milktea.model.user

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.nickname.UserNickname
import javax.annotation.concurrent.Immutable

/**
 * Userはfollowやunfollowなどは担当しない
 * Userはfollowやunfollowに関連しないため
 */
sealed interface User : Entity {

    val id: Id
    val userName: String
    val name: String?
    val avatarUrl: String?
    val emojis: List<Emoji>
    val isCat: Boolean?
    val isBot: Boolean?
    val host: String?
    val nickname: UserNickname?


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
        override val nickname: UserNickname?,
    ) : User {
        companion object
    }

    data class Detail(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<Emoji>,
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


    val displayUserName: String
        get() = "@" + this.userName + if(this.host == null){
            ""
        }else{
            "@" + this.host
        } 

    val displayName: String
        get() = nickname?.name?: name?: userName

    
    val shortDisplayName: String
        get() = "@" + this.userName

    fun getProfileUrl(account: Account): String {
        return "https://${account.getHost()}/${displayUserName}"
    }
}

enum class FollowState {
    PENDING_FOLLOW_REQUEST, FOLLOWING, UNFOLLOWING, UNFOLLOWING_LOCKED
}

fun User.Simple.Companion.make(
    id: User.Id,
    userName: String,
    name: String? = null,
    avatarUrl: String? = null,
    emojis: List<Emoji> = emptyList(),
    isCat: Boolean? = null,
    isBot: Boolean? = null,
    host: String? = null,
    nickname: UserNickname? = null,
): User.Simple {
    return User.Simple(
        id,
        userName = userName,
        name = name,
        avatarUrl = avatarUrl,
        emojis = emojis,
        isCat = isCat,
        isBot = isBot,
        host = host,
        nickname = nickname
    )
}
