package net.pantasystem.milktea.data.api.misskey.notes

data class FindRenotes (
    val i: String,
    val noteId: String,
    val untilId: String? = null,
    val sinceId: String? = null
)
