package net.pantasystem.milktea.api.misskey.users

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.data.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.model.emoji.Emoji
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
