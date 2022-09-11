package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.Serializable

@Serializable
data class CreateFolder(
    val i: String,
    val name: String,
    val parentId: String?
)