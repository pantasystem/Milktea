package jp.panta.misskeyandroidclient.model.users

import com.google.gson.annotations.SerializedName

data class RequestUser(
    val i: String,
    val userId: String?,
    @SerializedName("username") val userName: String? = null,
    val host: String? = null,
    val userIds: List<String>? = null
)