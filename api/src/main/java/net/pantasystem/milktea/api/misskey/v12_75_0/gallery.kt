package net.pantasystem.milktea.api.misskey.v12_75_0

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.serializations.DateSerializer
import java.util.*


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
data class GalleryPost @OptIn(ExperimentalSerializationApi::class) constructor(
    val id: String,
    @Serializable(with = DateSerializer::class) val createdAt: Date,
    @Serializable(with = DateSerializer::class) val updatedAt: Date,
    val title: String,
    val description: String,
    val userId: String,
    val user: UserDTO,
    val files: List<FilePropertyDTO>,
    val tags: List<String>?,
    val isSensitive: Boolean,
    val likedCount: Int?,
    val isLiked: Boolean?
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


