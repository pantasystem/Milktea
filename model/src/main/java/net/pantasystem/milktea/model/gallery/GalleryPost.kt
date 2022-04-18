package net.pantasystem.milktea.model.gallery


import net.pantasystem.milktea.model.EntityId
import java.util.*

sealed class GalleryPost {
    data class Id(
        val accountId: Long,
        val galleryId: String
    ) : EntityId

    abstract val id: Id
    abstract val createdAt: Date
    abstract val updatedAt: Date
    abstract val title: String
    abstract val description: String?
    abstract val userId: net.pantasystem.milktea.model.user.User.Id
    abstract val fileIds: List<net.pantasystem.milktea.model.drive.FileProperty.Id>
    abstract val tags: List<String>
    abstract val isSensitive: Boolean

    data class Normal(
        override val id: Id,
        override val createdAt: Date,
        override val updatedAt: Date,
        override val title: String,
        override val description: String?,
        override val userId: net.pantasystem.milktea.model.user.User.Id,
        override val fileIds: List<net.pantasystem.milktea.model.drive.FileProperty.Id>,
        override val tags: List<String>,
        override val isSensitive: Boolean
    ) : GalleryPost()

    data class Authenticated(
        override val id: Id,
        override val createdAt: Date,
        override val updatedAt: Date,
        override val title: String,
        override val description: String?,
        override val userId: net.pantasystem.milktea.model.user.User.Id,
        override val fileIds: List<net.pantasystem.milktea.model.drive.FileProperty.Id>,
        override val tags: List<String>,
        override val isSensitive: Boolean,
        val likedCount: Int,
        val isLiked: Boolean
    ) : GalleryPost()
}



data class GalleryPostRelation(
    val galleryPost: GalleryPost,
    val files: List<net.pantasystem.milktea.model.drive.FileProperty>,
    val user: net.pantasystem.milktea.model.user.User,
)