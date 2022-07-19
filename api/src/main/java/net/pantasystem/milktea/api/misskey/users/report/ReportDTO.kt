package net.pantasystem.milktea.api.misskey.users.report

import kotlinx.serialization.Serializable

@Serializable
data class ReportDTO(
    val i: String,
    val comment: String,
    val userId: String
)