package net.pantasystem.milktea.model.drive

import kotlinx.serialization.Serializable

@Serializable
data class Directory(
    val id: String,
    val createdAt: String,
    val name: String,
    val foldersCount: Int? = null,
    val filesCount: Int? = null,
    val parentId: String? = null,
    val parent: Directory? = null
)