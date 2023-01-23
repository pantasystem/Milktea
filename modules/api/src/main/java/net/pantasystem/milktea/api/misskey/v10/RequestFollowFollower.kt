package net.pantasystem.milktea.api.misskey.v10

import kotlinx.serialization.Serializable

@Serializable
data class RequestFollowFollower(
    val i: String?,
    val userId: String?,
    val cursor: String? = null,
    val username: String? = null,
    val host: String? = null,
    val limit: Int = 20,
    val iknow: Boolean? = null,
    val diff: Boolean? = null

)