package net.pantasystem.milktea.model.drive

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

import java.io.Serializable as JSerializable

data class FileProperty(
    val id: Id,
    val name: String,
    val createdAt: Instant?,
    val type: String,
    val md5: String?,
    val size: Int?,
    val userId: User.Id? = null,
    val folderId: String? = null,
    val comment: String? = null,
    val properties: Properties? = null,
    val isSensitive: Boolean = false,
    val blurhash: String? = null,
    val url: String,
    val thumbnailUrl: String? = null,
) : JSerializable {
    data class Id(
        val accountId: Long,
        val fileId: String,
    ) : JSerializable

    data class Properties(
        val width: Float?,
        val height: Float?,
    ) : JSerializable

    fun update(
        name: String = requireNotNull(this.name),
        comment: String? = null,
        isSensitive: Boolean? = null,
        folderId: String? = null,
    ): UpdateFileProperty {
        return UpdateFileProperty(
            fileId = id,
            name = if (name == this.name) null else ValueType.Some(name),
            comment = when (comment) {
                this.comment -> {
                    null
                }
                null -> {
                    ValueType.Empty()
                }
                else -> {
                    ValueType.Some(comment)
                }
            },
            isSensitive = when (isSensitive) {
                this.isSensitive -> {
                    null
                }
                null -> {
                    null
                }
                else -> {
                    ValueType.Some(isSensitive)
                }
            },
            folderId = when (folderId) {
                this.folderId -> {
                    null
                }
                null -> {
                    ValueType.Empty()
                }
                else -> {
                    ValueType.Some(folderId)
                }
            },
        )
    }
}