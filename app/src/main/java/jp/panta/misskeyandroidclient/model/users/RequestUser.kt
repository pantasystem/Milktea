package jp.panta.misskeyandroidclient.model.users

import com.google.gson.annotations.SerializedName

data class RequestUser(
    val i: String?,
    val userId: String?,
    @SerializedName("username") val userName: String? = null,
    val host: String? = null,
    val sort: String? = null,
    val state: String? = null,
    val origin: String? = null,
    val userIds: List<String>? = null,
    val sinceId: String? = null,
    val untilId: String? = null,
    val limit: Int = 21,
    val query: String? = null
)