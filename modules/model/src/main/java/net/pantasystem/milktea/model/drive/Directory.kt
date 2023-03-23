package net.pantasystem.milktea.model.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Directory(
    @SerialName("id") val id: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("name") val name: String,
    @SerialName("foldersCount") val foldersCount: Int? = null,
    @SerialName("filesCount") val filesCount: Int? = null,
    @SerialName("parentId") val parentId: String? = null,
    @SerialName("parent") val parent: Directory? = null
)