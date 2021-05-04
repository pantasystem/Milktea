package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*

data class FileProperty (
    val id: Id,
    val name: String,
    val createdAt: Date,
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
) {
    data class Id(
        val accountId: Long,
        val fileId: String
    )
    data class Properties(
        val width: Int,
        val height: Int
    )

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