package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.Serializable

@Serializable
data class RequestFolder(
    val i: String,
    val limit: Int? = null,
    val sinceId: String? = null,
    val untilId: String? = null,
    val folderId: String? = null,
    val name: String? = null,
    val parentId: String? = null


)