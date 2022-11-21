package net.pantasystem.milktea.model.user

import android.graphics.Color
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.nickname.UserNickname

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
    val host: String
    val nickname: UserNickname?
    val isSameHost: Boolean
    val instance: InstanceInfo?


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
        override val host: String,
        override val nickname: UserNickname?,
        override val isSameHost: Boolean,
        override val instance: InstanceInfo?
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
        override val host: String,
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
        override val isSameHost: Boolean,
        override val instance: InstanceInfo?,
        val birthday: LocalDate?,
        val fields: List<Field>,
        val createdAt: Instant?,
        val updatedAt: Instant?,
        val isPublicReactions: Boolean,
    ) : User {
        companion object

        val isRemoteUser: Boolean = !isSameHost

        val followState: FollowState
            get() {
                if (isFollowing) {
                    return FollowState.FOLLOWING
                }

                if (isLocked) {
                    return if (hasPendingFollowRequestFromYou) {
                        FollowState.PENDING_FOLLOW_REQUEST
                    } else {
                        FollowState.UNFOLLOWING_LOCKED
                    }
                }

                return FollowState.UNFOLLOWING
            }
        fun getRemoteProfileUrl(account: Account): String {
            return url ?: getProfileUrl(account)
        }
    }

    data class InstanceInfo(
        val faviconUrl: String?,
        val iconUrl: String?,
        val name: String?,
        val softwareName: String?,
        val softwareVersion: String?,
        val themeColor: String?,
    ) {

        val themeColorNumber: Result<Int?> by lazy {
            runCatching {
                themeColor?.let {
                    Color.parseColor(it)
                }
            }

        }
    }

    data class Field(
        val name: String,
        val value: String,
    )

    val displayUserName: String
        get() = "@" + this.userName + if (isSameHost) {
            ""
        } else {
            "@" + this.host
        }

    val displayName: String
        get() = nickname?.name ?: name ?: userName


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
    isSameHost: Boolean? = null,
    instance: User.InstanceInfo? = null,
): User.Simple {
    return User.Simple(
        id,
        userName = userName,
        name = name,
        avatarUrl = avatarUrl,
        emojis = emojis,
        isCat = isCat,
        isBot = isBot,
        host = host ?: "",
        nickname = nickname,
        isSameHost = isSameHost ?: false,
        instance = instance
    )
}


fun User.Detail.Companion.make(
    id: User.Id,
    userName: String,
    name: String? = null,
    avatarUrl: String? = null,
    emojis: List<Emoji> = emptyList(),
    isCat: Boolean? = null,
    isBot: Boolean? = null,
    host: String? = null,
    nickname: UserNickname? = null,
    description: String? = null,
    followersCount: Int? = null,
    followingCount: Int? = null,
    hostLower: String? = null,
    notesCount: Int? = null,
    pinnedNoteIds: List<Note.Id>? = null,
    bannerUrl: String? = null,
    url: String? = null,
    isFollowing: Boolean = false,
    isFollower: Boolean = false,
    isBlocking: Boolean = false,
    isMuting: Boolean = false,
    hasPendingFollowRequestFromYou: Boolean = false,
    hasPendingFollowRequestToYou: Boolean = false,
    isLocked: Boolean = false,
    isSameHost: Boolean = false,
    instance: User.InstanceInfo? = null,
    birthday: LocalDate? = null,
    fields: List<User.Field>? = null,
    createdAt: Instant? = null,
    updatedAt: Instant? = null,
    isPublicReactions: Boolean = false,
): User.Detail {
    return User.Detail(
        id,
        userName,
        name,
        avatarUrl,
        emojis,
        isCat,
        isBot,
        host ?: "",
        nickname,
        description,
        followersCount,
        followingCount,
        hostLower,
        notesCount,
        pinnedNoteIds,
        bannerUrl,
        url,
        isFollowing,
        isFollower,
        isBlocking,
        isMuting,
        hasPendingFollowRequestFromYou,
        hasPendingFollowRequestToYou,
        isLocked,
        isSameHost,
        instance,
        birthday,
        fields ?: emptyList(),
        createdAt,
        updatedAt,
        isPublicReactions = isPublicReactions,
    )
}