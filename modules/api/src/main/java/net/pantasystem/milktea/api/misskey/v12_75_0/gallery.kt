package net.pantasystem.milktea.api.misskey.v12_75_0

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO


@Serializable
data class CreateGallery(
    val i: String,
    val title: String,
    val description: String?,
    val fileIds: List<String>,
    val isSensitive: Boolean,
)

@Serializable
data class GetPosts(
    val i: String,
    val limit: Int = 10,
    val sinceId: String? = null,
    val untilId: String? = null,
    val userId: String? = null
)

@Serializable
data class GalleryPost(
    val id: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val title: String,
    val description: String? = null,
    val userId: String,
    val user: UserDTO,
    val files: List<FilePropertyDTO>,
    val tags: List<String>? = null,
    val isSensitive: Boolean,
    val likedCount: Int? = null,
    val isLiked: Boolean? = null
)

@Serializable
data class LikedGalleryPost(
    val id: String,
    val post: GalleryPost
)

@Serializable
data class Like(
    val i: String,
    val postId: String
)

@Serializable
data class UnLike(
    val i: String,
    val postId: String
)

@Serializable
data class Show(
    val i: String?,
    val postId: String
)

@Serializable
data class Update(
    val i: String,
    val postId: String,
    val title: String,
    val description: String?,
    val fileIds: List<String>,
    val isSensitive: Boolean
)


@Serializable
data class Delete(
    val i: String,
    val postId: String
)


