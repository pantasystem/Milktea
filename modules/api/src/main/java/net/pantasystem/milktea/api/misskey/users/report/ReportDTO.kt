package net.pantasystem.milktea.api.misskey.users.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportDTO(
    @SerialName("i")
    val i: String,

    @SerialName("comment")
    val comment: String,

    @SerialName("userId")
    val userId: String
)