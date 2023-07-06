package net.pantasystem.milktea.model.drive

data class Directory(
    val id: DirectoryId,
    val createdAt: String,
    val name: String,
    val foldersCount: Int? = null,
    val filesCount: Int? = null,
    val parentId: DirectoryId? = null,
    val parent: Directory? = null
)

data class DirectoryId(val accountId: Long, val directoryId: String)