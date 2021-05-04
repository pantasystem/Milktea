package jp.panta.misskeyandroidclient.api.users

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class UserDTO(
    val id:String,

    @SerializedName("username")
    @SerialName("username")
    val userName: String,

    val name: String? = null,
    val host: String? = null,
    val description: String? = null,
    //@SerializedName("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val hostLower: String? = null,
    val notesCount: Int? = null,
    //@JsonProperty("clientSettings") val clientSettings: ClientSetting? = null,
    val email: String? = null,
    val isBot: Boolean? = null,
    val isCat: Boolean? = null,
    //@SerializedName("lastUsedAt") val lastUsedAt: String? = null,
    //val line: String? = null,
    //@SerializedName("links") val links: String? = null,
    //@SerializedName("profile") val profile: Any? = null,
    //@SerializedName("settings") val settings: Any? = null,
    val pinnedNoteIds: List<String>? = null,
    val pinnedNotes: List<NoteDTO>? = null,
    //("twitter") val twitter: Any? = null,
    val twoFactorEnabled: Boolean? = null,
    val isAdmin: Boolean? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    //@SerializedName("avatarColor") val avatarColor: Any? = null,
    val emojis: List<Emoji>? = null,

    val isFollowing: Boolean? = null,
    val isFollowed: Boolean? = null,


    val isBlocking: Boolean? = null,
    val isMuted: Boolean? = null,
    val url: String? = null,
    val hasPendingFollowRequestFromYou: Boolean? = null,
    val hasPendingFollowRequestToYou: Boolean? = null

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
    if(isDetail){

        return User.Detail(
            id = User.Id(account.accountId, this.id),
            avatarUrl = this.avatarUrl,
            emojis = this.emojis?: emptyList(),
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
            pinnedNoteIds = this.pinnedNoteIds?.map{
                Note.Id(account.accountId, it)
            },
            isFollowing = this.isFollowing?: false,
            isFollower = this.isFollowed?: false,
            isBlocking = this.isBlocking?: false,
            isMuting = this.isMuted?: false,
            hasPendingFollowRequestFromYou = hasPendingFollowRequestFromYou?: false,
            hasPendingFollowRequestToYou = hasPendingFollowRequestToYou?: false
        )
    }else{
        return User.Simple(
            id = User.Id(account.accountId, this.id),
            avatarUrl = this.avatarUrl,
            emojis = this.emojis?: emptyList(),
            isBot = this.isBot,
            isCat = this.isCat,
            name = this.name,
            userName = this.userName,
            host = this.host
        )
    }

}