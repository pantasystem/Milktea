package jp.panta.misskeyandroidclient.model.users

import com.google.gson.annotations.SerializedName

data class Profile(
    val host: String?,
    val description: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val hostLower: String?,
    val notesCount: Int?,
    val pinnedNoteIds: List<String>?,
    val bannerUrl: String?,



    val url: String?

)