package net.pantasystem.milktea.api.misskey.users


import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.emoji.CustomEmojisTypeSerializer
import net.pantasystem.milktea.api.misskey.emoji.EmojisType
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.model.emoji.Emoji
import java.io.Serializable

/**
 * @param isLocked フォロー承認制
 */
@kotlinx.serialization.Serializable
data class UserDTO(
    val id: String,

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

    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
    @SerialName("emojis") val rawEmojis: EmojisType? = null,

    val isFollowing: Boolean? = null,
    val isFollowed: Boolean? = null,


    val isBlocking: Boolean? = null,
    val isMuted: Boolean? = null,
    val url: String? = null,
    val hasPendingFollowRequestFromYou: Boolean? = null,
    val hasPendingFollowRequestToYou: Boolean? = null,
    val isLocked: Boolean? = null,
    val instance: InstanceInfo? = null,
    val fields: List<FieldDTO>? = null,

    val birthday: LocalDate? = null,

    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val publicReactions: Boolean? = null,
    val avatarBlurhash: String? = null,
) : Serializable {

    @kotlinx.serialization.Serializable
    data class InstanceInfo(
        val faviconUrl: String? = null,
        val iconUrl: String? = null,
        val name: String? = null,
        val softwareName: String? = null,
        val softwareVersion: String? = null,
        val themeColor: String? = null,
    )

    val emojiList: List<Emoji>? = when(rawEmojis) {
        EmojisType.None -> null
        is EmojisType.TypeArray -> rawEmojis.emojis
        is EmojisType.TypeObject -> rawEmojis.emojis.map {
            Emoji(name = it.key, url = it.value, uri = it.value)
        }
        null -> null
    }

    @kotlinx.serialization.Serializable
    data class FieldDTO(val name: String, val value: String)

}
