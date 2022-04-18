package net.pantasystem.milktea.model.drive

import kotlinx.datetime.Instant
import net.pantasystem.milktea.data.model.file.File
import net.pantasystem.milktea.data.model.users.User
import java.io.Serializable as JSerializable


data class FileProperty (
    val id: Id,
    val name: String,
    val createdAt: Instant,
    val type: String,
    val md5: String,
    val size: Int,
    val userId: User.Id? = null,
    val folderId: String? = null,
    val comment: String? = null,
    val properties: Properties? = null,
    val isSensitive: Boolean = false,
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

    fun toFile(): File {


        return File(
            name,
            url,
            type,
            id,
            null,
            thumbnailUrl,
            isSensitive
        )
    }
}