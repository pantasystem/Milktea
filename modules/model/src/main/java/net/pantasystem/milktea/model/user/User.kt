package net.pantasystem.milktea.model.user

import android.graphics.Color
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.note.Note
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
    val emojis: List<CustomEmoji>
    val isCat: Boolean?
    val isBot: Boolean?
    val host: String
    val nickname: UserNickname?
    val isSameHost: Boolean
    val instance: InstanceInfo?
    val avatarBlurhash: String?
    val parsedResult: CustomEmojiParsedResult
    val badgeRoles: List<BadgeRole>


    class Id(
        val accountId: Long,
        val id: String,
    ) : EntityId {

        private var _hashCode: Int? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Id

            if (accountId != other.accountId) return false
            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            if (_hashCode != null) return _hashCode!!
            var result = accountId.hashCode()
            result = 31 * result + id.hashCode()
            _hashCode = result
            return result
        }

        override fun toString(): String {
            return "Id(accountId=$accountId, id='$id')"
        }

    }

    data class Simple(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<CustomEmoji>,
        override val isCat: Boolean?,
        override val isBot: Boolean?,
        override val host: String,
        override val nickname: UserNickname?,
        override val isSameHost: Boolean,
        override val instance: InstanceInfo?,
        override val avatarBlurhash: String?,
        override val badgeRoles: List<BadgeRole>
    ) : User {
        companion object;
        override val parsedResult: CustomEmojiParsedResult = try {
            CustomEmojiParser.parse(sourceHost = host, emojis, displayName)
        } catch (e: Throwable) {
            CustomEmojiParsedResult(displayName, emptyList())
        }

        override val iconBadgeRoles: List<BadgeRole> = badgeRoles.filter { it.iconUri != null }.sortedByDescending {
            it.displayOrder
        }

    }

    data class Detail(
        override val id: Id,
        override val userName: String,
        override val name: String?,
        override val avatarUrl: String?,
        override val emojis: List<CustomEmoji>,
        override val isCat: Boolean?,
        override val isBot: Boolean?,
        override val host: String,
        override val nickname: UserNickname?,
        override val avatarBlurhash: String?,
        override val isSameHost: Boolean,
        override val instance: InstanceInfo?,
        override val badgeRoles: List<BadgeRole>,
        val info: Info,
        val related: Related?,
    ) : User {
        companion object

        val isRemoteUser: Boolean = !isSameHost

        val followState: FollowState
            get() {
                if (related?.isFollowing == true) {
                    return FollowState.FOLLOWING
                }

                if (info.isLocked) {
                    return if (related?.hasPendingFollowRequestFromYou == true) {
                        FollowState.PENDING_FOLLOW_REQUEST
                    } else {
                        FollowState.UNFOLLOWING_LOCKED
                    }
                }

                return FollowState.UNFOLLOWING
            }

        fun getRemoteProfileUrl(account: Account): String {
            return info.url ?: getProfileUrl(account)
        }

        override val parsedResult: CustomEmojiParsedResult = try {
            CustomEmojiParser.parse(sourceHost = host, emojis, displayName)
        } catch (e: Throwable) {
            CustomEmojiParsedResult(displayName, emptyList())
        }

        override val iconBadgeRoles: List<BadgeRole> = badgeRoles.filter { it.iconUri != null }.sortedByDescending {
            it.displayOrder
        }
    }

    data class Info(
        val description: String?,
        val followersCount: Int?,
        val followingCount: Int?,
        val hostLower: String?,
        val notesCount: Int?,
        val pinnedNoteIds: List<Note.Id>?,
        val bannerUrl: String?,
        val url: String?,
        val isLocked: Boolean,
        val birthday: LocalDate?,
        val fields: List<Field>,
        val createdAt: Instant?,
        val updatedAt: Instant?,
        val isPublicReactions: Boolean,
        val ffVisibility: FollowerFollowerVisibility?,
    )

    data class Related(
        val isFollowing: Boolean,
        val isFollower: Boolean,
        val isBlocking: Boolean,
        val isMuting: Boolean,
        val hasPendingFollowRequestFromYou: Boolean,
        val hasPendingFollowRequestToYou: Boolean,
        val isNotify: Boolean?,
    )

    data class InstanceInfo(
        val faviconUrl: String?,
        val iconUrl: String?,
        val name: String?,
        val softwareName: String?,
        val softwareVersion: String?,
        val themeColor: String?,
    ) {

        val themeColorNumber: Result<Int?> by lazy {
            runCancellableCatching {
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

    data class BadgeRole(
        val name: String,
        val iconUri: String?,
        val displayOrder: Int,
    )

    enum class FollowerFollowerVisibility {
        Public, Followers, Private,
    }

    val displayUserName: String
        get() = "@" + this.userName + if (isSameHost) {
            ""
        } else {
            "@" + this.host
        }

    val iconBadgeRoles: List<BadgeRole>
        get() = badgeRoles.filter { it.iconUri != null }

    val displayName: String
        get() = nickname?.name ?: name ?: userName


    val shortDisplayName: String
        get() = "@" + this.userName

    fun getProfileUrl(account: Account): String {
        return "https://${account.getHost()}/${displayUserName}"
    }

    fun castAndPartiallyFill(): Detail {
        when(this) {
            is Detail -> return this
            is Simple -> return Detail(
                id = id,
                userName = userName,
                name = name,
                avatarUrl = avatarUrl,
                emojis = emojis,
                isCat = isCat,
                isBot = isBot,
                host = host,
                nickname = nickname,
                avatarBlurhash = avatarBlurhash,
                isSameHost = isSameHost,
                instance = instance,
                info = Info(
                    description = null,
                    followersCount = null,
                    followingCount = null,
                    hostLower = null,
                    notesCount = null,
                    pinnedNoteIds = null,
                    bannerUrl = null,
                    url = null,
                    isLocked = false,
                    birthday = null,
                    fields = emptyList(),
                    createdAt = null,
                    updatedAt = null,
                    isPublicReactions = false,
                    ffVisibility = null,
                ),
                related = null,
                badgeRoles = badgeRoles,
            )
        }
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
    emojis: List<CustomEmoji> = emptyList(),
    isCat: Boolean? = null,
    isBot: Boolean? = null,
    host: String? = null,
    nickname: UserNickname? = null,
    isSameHost: Boolean? = null,
    instance: User.InstanceInfo? = null,
    avatarBlurhash: String? = null,
    badgeRoles: List<User.BadgeRole> = emptyList(),
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
        instance = instance,
        avatarBlurhash = avatarBlurhash,
        badgeRoles = badgeRoles,
    )
}


fun User.Detail.Companion.make(
    id: User.Id,
    userName: String,
    name: String? = null,
    avatarUrl: String? = null,
    emojis: List<CustomEmoji> = emptyList(),
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
    avatarBlurhash: String? = null,
    isNotify: Boolean = false,
    badgeRoles: List<User.BadgeRole> = emptyList(),
    ffVisibility: User.FollowerFollowerVisibility? = null,
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
        avatarBlurhash,
        instance = instance,
        isSameHost = isSameHost,
        info = User.Info(
            description = description,
            followersCount = followersCount,
            followingCount = followingCount,
            hostLower = hostLower,
            notesCount = notesCount,
            pinnedNoteIds = pinnedNoteIds,
            bannerUrl = bannerUrl,
            url = url,
            isLocked = isLocked,
            birthday = birthday,
            fields = fields ?: emptyList(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPublicReactions = isPublicReactions,
            ffVisibility = ffVisibility,
        ),
        related = User.Related(
            isFollowing = isFollowing,
            isFollower = isFollower,
            isBlocking = isBlocking,
            isMuting = isMuting,
            hasPendingFollowRequestFromYou = hasPendingFollowRequestFromYou,
            hasPendingFollowRequestToYou = hasPendingFollowRequestToYou,
            isNotify = isNotify,
        ),
        badgeRoles = badgeRoles,
    )
}