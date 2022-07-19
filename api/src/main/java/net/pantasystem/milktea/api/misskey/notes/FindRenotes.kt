package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable
data class FindRenotes (
    val i: String,
    val noteId: String,
    val untilId: String? = null,
    val sinceId: String? = null
)
