package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*

data class GalleryPost (
    val id: Id,
    val createdAt: Date,
    val updatedAt: Date,
    val title: String,
    val description: String?,
    val userId: User.Id,
    val fileIds: List<FileProperty.Id>,
    val tags: List<String>,
    val isSensitive: Boolean
) {
    data class Id(
        val accountId: Long,
        val galleryId: String
    ) : EntityId
}