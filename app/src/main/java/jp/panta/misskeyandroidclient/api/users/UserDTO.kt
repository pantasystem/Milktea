package jp.panta.misskeyandroidclient.api.users

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.users.Profile
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class UserDTO(
    val id:String,

    @SerializedName("username")
    @SerialName("username")
    val userName: String,

    val name: String?,
    val host: String?,
    val description: String?,
    //@SerializedName("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date?,
    val followersCount: Int?,
    val followingCount: Int?,
    val hostLower: String?,
    val notesCount: Int?,
    //@JsonProperty("clientSettings") val clientSettings: ClientSetting?,
    val email: String?,
    val isBot: Boolean,
    val isCat: Boolean,
    //@SerializedName("lastUsedAt") val lastUsedAt: String?,
    //val line: String?,
    //@SerializedName("links") val links: String?,
    //@SerializedName("profile") val profile: Any?,
    //@SerializedName("settings") val settings: Any?,
    val pinnedNoteIds: List<String>?,
    val pinnedNotes: List<NoteDTO>?,
    //("twitter") val twitter: Any?,
    val twoFactorEnabled: Boolean?,
    val isAdmin: Boolean?,
    val avatarUrl: String?,
    val bannerUrl: String?,
    //@SerializedName("avatarColor") val avatarColor: Any?,
    val emojis: List<Emoji>?,

    val isFollowing: Boolean?,
    val isFollowed: Boolean?,


    val isBlocking: Boolean?,
    val isMuted: Boolean?,
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

fun UserDTO.toUser(account: Account, isDetail: Boolean = false): User{
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
            notesCount = this.notesCount,
            pinnedNoteIds = this.pinnedNoteIds
        )
    }
    return User(
        id = User.Id(account.accountId, this.id),
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