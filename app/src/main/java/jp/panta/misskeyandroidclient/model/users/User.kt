package jp.panta.misskeyandroidclient.model.users

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.Note
import java.io.Serializable

data class User(
    @SerializedName("id") val id:String,
    @SerializedName("username") val userName: String,
    @SerializedName("name") val name: String?,
    @SerializedName("host") val host: String?,
    @SerializedName("description") val description: String?,
    //@SerializedName("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date?,
    @SerializedName("followersCount") val followersCount: Int,
    @SerializedName("followingCount") val followingCount: Int,
    @SerializedName("hostLower") val hostLower: String?,
    @SerializedName("notesCount") val notesCount: Int,
    //@JsonProperty("clientSettings") val clientSettings: ClientSetting?,
    @SerializedName("email") val email: String?,
    @SerializedName("isBot") val isBot: Boolean,
    @SerializedName("isCat") val isCat: Boolean,
    //@SerializedName("lastUsedAt") val lastUsedAt: String?,
    @SerializedName("line") val line: String?,
    @SerializedName("links") val links: String?,
    @SerializedName("profile") val profile: Any?,
    //@SerializedName("settings") val settings: Any?,
    @SerializedName("pinnedNoteIds") val pinnedNoteIds: List<String>?,
    @SerializedName("pinnedNotes") val pinnedNotes: List<Note>?,
    //("twitter") val twitter: Any?,
    //erty("twoFactorEnabled") val twoFactorEnabled: Any?,
    @SerializedName("isAdmin") val isAdmin: Boolean,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("bannerUrl") val bannerUrl: String?,
    //@SerializedName("avatarColor") val avatarColor: Any?,
    @SerializedName("emojis") val emojis: List<Emoji>?
    //JsonProperty("isVerified") val isVerified: Boolean,
    //@JsonProperty("isLocked") val isLocked: Boolean
): Serializable