package net.pantasystem.milktea.model.drive

data class CreateDirectory(
    val accountId: Long,
    val directoryName: String,
    val parentId: String? = null,
)