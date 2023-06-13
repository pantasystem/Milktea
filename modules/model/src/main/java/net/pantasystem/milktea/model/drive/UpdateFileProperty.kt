package net.pantasystem.milktea.model.drive

data class UpdateFileProperty(
    val fileId: FileProperty.Id,
    val folderId: String?,
    val name: String,
    val isSensitive: Boolean,
    val comment: String?,
)

sealed interface ValueType<T> {
    class Empty<T> : ValueType<T>

    data class Some<T>(val value: T) : ValueType<T>

}
