package net.pantasystem.milktea.api.misskey.v10

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestFollowFollower(
    @SerialName("i")
    val i: String?,

    @SerialName("userId")
    val userId: String?,

    @SerialName("cursor")
    val cursor: String? = null,

    @SerialName("username")
    val username: String? = null,

    @SerialName("host")
    val host: String? = null,

    @SerialName("limit")
    val limit: Int = 20,

    @SerialName("iknow")
    val iknow: Boolean? = null,

    @SerialName("diff")
    val diff: Boolean? = null

)