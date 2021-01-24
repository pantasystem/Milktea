package jp.panta.misskeyandroidclient.api.users

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.users.Profile
import jp.panta.misskeyandroidclient.model.users.User
import java.io.Serializable

data class UserDTO(
    @SerializedName("id") val id:String,
    @SerializedName("username") val userName: String,
    @SerializedName("name") val name: String?,
    @SerializedName("host") val host: String?,
    @SerializedName("description") val description: String?,
    //@SerializedName("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date?,
    @SerializedName("followersCount") val followersCount: Int?,
    @SerializedName("followingCount") val followingCount: Int?,
    @SerializedName("hostLower") val hostLower: String?,
    @SerializedName("notesCount") val notesCount: Int?,
    //@JsonProperty("clientSettings") val clientSettings: ClientSetting?,
    @SerializedName("email") val email: String?,
    @SerializedName("isBot") val isBot: Boolean,
    @SerializedName("isCat") val isCat: Boolean,
    //@SerializedName("lastUsedAt") val lastUsedAt: String?,
    @SerializedName("line") val line: String?,
    @SerializedName("links") val links: String?,
    //@SerializedName("profile") val profile: Any?,
    //@SerializedName("settings") val settings: Any?,
    @SerializedName("pinnedNoteIds") val pinnedNoteIds: List<String>?,
    @SerializedName("pinnedNotes") val pinnedNotes: List<NoteDTO>?,
    //("twitter") val twitter: Any?,
    val twoFactorEnabled: Boolean?,
    @SerializedName("isAdmin") val isAdmin: Boolean?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("bannerUrl") val bannerUrl: String?,
    //@SerializedName("avatarColor") val avatarColor: Any?,
    @SerializedName("emojis") val emojis: List<Emoji>?,

    @SerializedName("isFollowing") val isFollowing: Boolean?,
    @SerializedName("isFollowed") val isFollowed: Boolean?,


    @SerializedName("isBlocking") val isBlocking: Boolean?,
    @SerializedName("isMuted") val isMuted: Boolean?,
    val url: String?

    //JsonProperty("isVerified") val isVerified: Boolean,
    //@JsonProperty("isLocked") val isLocked: Boolean
): Serializable{
    fun getDisplayUserName(): String{
        return "@" + this.userName + if(this.host == null){
            ""
        }else{
            "@" + this.host
        }
    }

    fun getDisplayName(): String{
        return name?: userName
    }

    fun getShortDisplayName(): String{
        return "@" + this.userName
    }
}

fun UserDTO.toUser(isDetail: Boolean = false): User{
    var state: User.State? = null
    var profile: Profile? = null
    if(isDetail){
        state = User.State(
            isFollowing = this.isFollowing?: false,
            isFollower = this.isFollowed?: false,
            isBlocking = this.isBlocking?: false,
            isMuting = this.isMuted?: false
        )

        profile = Profile(
            bannerUrl = this.bannerUrl,
            description = this.description,
            followersCount = this.followersCount,
            followingCount = this.followingCount,
            host = this.host,
            url = this.url,
            hostLower = this.hostLower,
            links = this.links,
            notesCount = this.notesCount,
            pinnedNoteIds = this.pinnedNoteIds
        )
    }
    return User(
        id = this.id,
        avatarUrl = this.avatarUrl,
        emojis = this.emojis?: emptyList(),
        isBot = this.isBot,
        isCat = this.isCat,
        name = this.name,
        userName = this.userName,
        state = state,
        profile = profile
    )
}