package net.pantasystem.milktea.api.misskey.users


import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.emoji.CustomEmojisTypeSerializer
import net.pantasystem.milktea.api.misskey.emoji.EmojisType
import net.pantasystem.milktea.api.misskey.emoji.TypeObjectValueType
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.model.emoji.Emoji
import java.io.Serializable

/**
 * @param isLocked フォロー承認制
 */
@kotlinx.serialization.Serializable
data class UserDTO(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val userName: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("host")
    val host: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("followersCount")
    val followersCount: Int? = null,

    @SerialName("followingCount")
    val followingCount: Int? = null,

    @SerialName("hostLower")
    val hostLower: String? = null,

    @SerialName("notesCount")
    val notesCount: Int? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("isBot")
    val isBot: Boolean? = null,

    @SerialName("isCat")
    val isCat: Boolean? = null,

    @SerialName("pinnedNoteIds")
    val pinnedNoteIds: List<String>? = null,

    @SerialName("pinnedNotes")
    val pinnedNotes: List<NoteDTO>? = null,

    @SerialName("twoFactorEnabled")
    val twoFactorEnabled: Boolean? = null,

    @SerialName("isAdmin")
    val isAdmin: Boolean? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null,

    @SerialName("bannerUrl")
    val bannerUrl: String? = null,

    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
    @SerialName("emojis")
    val rawEmojis: EmojisType? = null,

    @SerialName("isFollowing")
    val isFollowing: Boolean? = null,

    @SerialName("isFollowed")
    val isFollowed: Boolean? = null,

    @SerialName("isBlocking")
    val isBlocking: Boolean? = null,

    @SerialName("isMuted")
    val isMuted: Boolean? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("hasPendingFollowRequestFromYou")
    val hasPendingFollowRequestFromYou: Boolean? = null,

    @SerialName("hasPendingFollowRequestToYou")
    val hasPendingFollowRequestToYou: Boolean? = null,

    @SerialName("isLocked")
    val isLocked: Boolean? = null,

    @SerialName("instance")
    val instance: InstanceInfo? = null,

    @SerialName("fields")
    val fields: List<FieldDTO>? = null,

    @SerialName("birthday")
    @kotlinx.serialization.Transient
    val birthday: LocalDate? = null,

    @SerialName("createdAt")
    val createdAt: Instant? = null,

    @SerialName("updatedAt")
    val updatedAt: Instant? = null,

    @SerialName("publicReactions")
    val publicReactions: Boolean? = null,

    @SerialName("avatarBlurhash")
    val avatarBlurhash: String? = null,
) : Serializable {

    @kotlinx.serialization.Serializable
    data class InstanceInfo(
        @SerialName("faviconUrl")
        val faviconUrl: String? = null,

        @SerialName("iconUrl")
        val iconUrl: String? = null,

        @SerialName("name")
        val name: String? = null,

        @SerialName("softwareName")
        val softwareName: String? = null,

        @SerialName("softwareVersion")
        val softwareVersion: String? = null,

        @SerialName("themeColor")
        val themeColor: String? = null,
    )

    val emojiList: List<Emoji>? = when (rawEmojis) {
        EmojisType.None -> null
        is EmojisType.TypeArray -> rawEmojis.emojis
        is EmojisType.TypeObject -> rawEmojis.emojis.map {
            Emoji(
                name = it.key,
                uri = when(val v = it.value) {
                    is TypeObjectValueType.Obj -> v.emoji.uri
                    is TypeObjectValueType.Value -> v.value
                },
                url = when(val v = it.value) {
                    is TypeObjectValueType.Obj -> v.emoji.url
                    is TypeObjectValueType.Value -> v.value
                }
            )
        }
        null -> null
    }

    @kotlinx.serialization.Serializable
    data class FieldDTO(
        @SerialName("name")
        val name: String,

        @SerialName("value")
        val value: String,
    )

}
