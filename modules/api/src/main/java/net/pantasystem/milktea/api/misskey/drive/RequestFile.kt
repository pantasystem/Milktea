package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestFile (
    @SerialName("i")
    val i: String,

    @SerialName("limit")
    val limit: Int? = null,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("folderId")
    val folderId: String? = null,

    @SerialName("type")
    val type: String? = null
)