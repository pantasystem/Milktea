package net.pantasystem.milktea.data.infrastructure.user.db

import net.pantasystem.milktea.model.user.User

data class UserRecord(
    val id: User.Id,
    val userName: String,
    val name: String?,
    val avatarUrl: String?,
    val isCat: Boolean?,
    val isBot: Boolean?,
    val host: String,
    val isSameHost: Boolean

)

data class UserEmoji(
    val name: String,
    val url: String?,
    val uri: String?,
    val userId: Long,
    val id: Long
)

data class UserDetailedState(
    val description: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val hostLower: String?,
    val notesCount: Int?,
    val bannerUrl: String?,
    val url: String?,
    val isFollowing: Boolean,
    val isFollower: Boolean,
    val isBlocking: Boolean,
    val isMuting: Boolean,
    val hasPendingFollowRequestFromYou: Boolean,
    val hasPendingFollowRequestToYou: Boolean,
    val isLocked: Boolean,
)

data class PinnedNoteId(
    val noteId: String,
    val userId: Long
)