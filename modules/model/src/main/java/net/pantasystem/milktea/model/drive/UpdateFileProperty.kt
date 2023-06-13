package net.pantasystem.milktea.model.drive

data class UpdateFileProperty(
    val fileId: FileProperty.Id,
    val folderId: ValueType<String>? = null,
    val name: ValueType<String>? = null,
    val isSensitive: ValueType<Boolean>? = null,
    val comment: ValueType<String>? = null,
)

sealed interface ValueType<T> {
    class Empty<T> : ValueType<T>

    data class Some<T>(val value: T) : ValueType<T>

}
