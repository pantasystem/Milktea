package jp.panta.misskeyandroidclient.model.users

import com.google.gson.annotations.SerializedName

data class Profile(
    val userId: String,
    val host: String?,
    val description: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val hostLower: String?,
    val notesCount: Int?,
    val links: String?,
    val pinnedNoteIds: List<String>?,
    val isAdmin: Boolean?,
    val bannerUrl: String?,

    val isFollowing: Boolean?,
    val isFollowed: Boolean?,


    val isBlocking: Boolean?,
    val isMuted: Boolean?,
    val url: String?

)