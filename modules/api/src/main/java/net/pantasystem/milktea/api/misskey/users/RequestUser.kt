package net.pantasystem.milktea.api.misskey.users


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.user.query.FindUsersQuery

@Serializable
data class RequestUser(
    val i: String?,
    val userId: String? = null,
    @SerialName("username") val userName: String? = null,
    val host: String? = null,
    val sort: String? = null,
    val state: String? = null,
    val origin: String? = null,
    val userIds: List<String>? = null,
    val sinceId: String? = null,
    val untilId: String? = null,
    val limit: Int? = null,
    val query: String? = null,
    val detail: Boolean? = null,
) {

    companion object


}

fun RequestUser.Companion.from(query: FindUsersQuery, i: String): RequestUser {
    return RequestUser(
        i = i,
        origin = query.origin?.origin,
        sort = query.sort?.str(),
        state = query.state?.state
    )

}