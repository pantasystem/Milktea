package net.pantasystem.milktea.api.misskey.v12_75_0

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO


@Serializable
data class CreateGallery(
    @SerialName("i")
    val i: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String?,

    @SerialName("fileIds")
    val fileIds: List<String>,

    @SerialName("isSensitive")
    val isSensitive: Boolean,
)

@Serializable
data class GetPosts(
    @SerialName("i")
    val i: String,

    @SerialName("limit")
    val limit: Int = 10,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("userId")
    val userId: String? = null
)

@Serializable
data class GalleryPost(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("updatedAt")
    val updatedAt: Instant,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("userId")
    val userId: String,

    @SerialName("user")
    val user: UserDTO,

    @SerialName("files")
    val files: List<FilePropertyDTO>,

    @SerialName("tags")
    val tags: List<String>? = null,

    @SerialName("isSensitive")
    val isSensitive: Boolean,

    @SerialName("likedCount")
    val likedCount: Int? = null,

    @SerialName("isLiked")
    val isLiked: Boolean? = null
)

@Serializable
data class LikedGalleryPost(
    @SerialName("id")
    val id: String,

    @SerialName("post")
    val post: GalleryPost
)

@Serializable
data class Like(
    @SerialName("i")
    val i: String,

    @SerialName("postId")
    val postId: String
)

@Serializable
data class UnLike(
    @SerialName("i")
    val i: String,

    @SerialName("postId")
    val postId: String
)

@Serializable
data class Show(
    @SerialName("i")
    val i: String?,

    @SerialName("postId")
    val postId: String
)

@Serializable
data class Update(
    @SerialName("i")
    val i: String,

    @SerialName("postId")
    val postId: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String?,

    @SerialName("fileIds")
    val fileIds: List<String>,

    @SerialName("isSensitive")
    val isSensitive: Boolean
)


@Serializable
data class Delete(
    @SerialName("i")
    val i: String,

    @SerialName("postId")
    val postId: String
)


