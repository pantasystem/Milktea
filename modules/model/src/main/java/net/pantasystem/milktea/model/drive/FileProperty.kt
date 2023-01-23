package net.pantasystem.milktea.model.drive

import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.user.User

import java.io.Serializable as JSerializable

data class FileProperty (
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
) : JSerializable{
    data class Id(
        val accountId: Long,
        val fileId: String
    ) : JSerializable
    data class Properties(
        val width: Float?,
        val height: Float?
    ) : JSerializable

    fun update(
        name: String = requireNotNull(this.name),
        comment: String? = this.comment,
        isSensitive: Boolean = this.isSensitive,
        folderId: String? = this.folderId,
    ): UpdateFileProperty {
        return UpdateFileProperty(
            name = name,
            comment = comment,
            fileId = this.id,
            isSensitive = isSensitive,
            folderId = folderId,
        )
    }
}