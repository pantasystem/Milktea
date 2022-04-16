package net.pantasystem.milktea.data.api.misskey.users

import com.google.gson.annotations.SerializedName
import net.pantasystem.milktea.data.model.emoji.Emoji
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.notes.Note
import net.pantasystem.milktea.data.model.users.User
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.data.api.misskey.notes.NoteDTO
import java.io.Serializable

/**
 * @param isLocked フォロー承認制
 */
@kotlinx.serialization.Serializable
data class UserDTO(
    val id: String,

    @SerializedName("username")
    @SerialName("username")
    val userName: String,

    val name: String? = null,
    val host: String? = null,
    val description: String? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val hostLower: String? = null,
    val notesCount: Int? = null,
    val email: String? = null,
    val isBot: Boolean? = null,
    val isCat: Boolean? = null,
    val pinnedNoteIds: List<String>? = null,
    val pinnedNotes: List<NoteDTO>? = null,
    val twoFactorEnabled: Boolean? = null,
    val isAdmin: Boolean? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val emojis: List<Emoji>? = null,

    val isFollowing: Boolean? = null,
    val isFollowed: Boolean? = null,


    val isBlocking: Boolean? = null,
    val isMuted: Boolean? = null,
    val url: String? = null,
    val hasPendingFollowRequestFromYou: Boolean? = null,
    val hasPendingFollowRequestToYou: Boolean? = null,
    val isLocked: Boolean? = null
) : Serializable {
    fun getDisplayUserName(): String {
        return "@" + this.userName + if (this.host == null) {
            ""
        } else {
            "@" + this.host
        }
    }

    fun getDisplayName(): String {
        return name ?: userName
    }

    fun getShortDisplayName(): String {
        return "@" + this.userName
    }
}

fun UserDTO.toUser(account: Account, isDetail: Boolean = false): User {
    if (isDetail) {

        return User.Detail(
            id = User.Id(account.accountId, this.id),
            avatarUrl = this.avatarUrl,
            emojis = this.emojis ?: emptyList(),
            isBot = this.isBot,
            isCat = this.isCat,
            name = this.name,
            userName = this.userName,
            bannerUrl = this.bannerUrl,
            description = this.description,
            followersCount = this.followersCount,
            followingCount = this.followingCount,
            host = this.host,
            url = this.url,
            hostLower = this.hostLower,
            notesCount = this.notesCount,
            pinnedNoteIds = this.pinnedNoteIds?.map {
                Note.Id(account.accountId, it)
            },
            isFollowing = this.isFollowing ?: false,
            isFollower = this.isFollowed ?: false,
            isBlocking = this.isBlocking ?: false,
            isMuting = this.isMuted ?: false,
            hasPendingFollowRequestFromYou = hasPendingFollowRequestFromYou ?: false,
            hasPendingFollowRequestToYou = hasPendingFollowRequestToYou ?: false,
            isLocked = isLocked ?: false,
            nickname = null,
        )
    } else {
        return User.Simple(
            id = User.Id(account.accountId, this.id),
            avatarUrl = this.avatarUrl,
            emojis = this.emojis ?: emptyList(),
            isBot = this.isBot,
            isCat = this.isCat,
            name = this.name,
            userName = this.userName,
            host = this.host,
            nickname = null
        )
    }

}