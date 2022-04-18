package net.pantasystem.milktea.model.drive

import kotlinx.serialization.Serializable

@Serializable
data class Directory(
    val id: String,
    val createdAt: String,
    val name: String,
    val foldersCount: Int,
    val filesCount: Int,
    val parentId: String?,
    val parent: Directory?
)