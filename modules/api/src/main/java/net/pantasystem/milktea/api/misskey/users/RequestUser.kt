package net.pantasystem.milktea.api.misskey.users


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.user.query.FindUsersQuery4Misskey

@Serializable
data class RequestUser(
    @SerialName("i")
    val i: String?,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("username")
    val userName: String? = null,

    @SerialName("host")
    val host: String? = null,

    @SerialName("sort")
    val sort: String? = null,

    @SerialName("state")
    val state: String? = null,

    @SerialName("origin")
    val origin: String? = null,

    @SerialName("userIds")
    val userIds: List<String>? = null,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("limit")
    val limit: Int? = null,

    @SerialName("query")
    val query: String? = null,

    @SerialName("detail")
    val detail: Boolean? = null,
) {

    companion object


}

fun RequestUser.Companion.from(query: FindUsersQuery4Misskey, i: String): RequestUser {
    return RequestUser(
        i = i,
        origin = query.origin?.origin,
        sort = query.sort?.str(),
        state = query.state?.state
    )

}