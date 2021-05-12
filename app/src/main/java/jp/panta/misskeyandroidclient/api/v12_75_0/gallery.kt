package jp.panta.misskeyandroidclient.api.v12_75_0

import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.util.*


@Serializable
data class CreateGallery(
    val i: String,
    val title: String,
    val description: String?,
    val fileIds: List<String>,
    val isSensitive: Boolean
)

@Serializable
data class GetPosts(
    val i: String,
    val limit: Int = 10,
    val sinceId: String? = null,
    val untilId: String? = null
)

@Serializable
data class GalleryPost(
    val id: String,
    @Serializable(with = DateSerializer::class) val createdAt: Date,
    @Serializable(with = DateSerializer::class) val updatedAt: Date,
    val title: String,
    val description: String,
    val userId: String,
    val user: UserDTO,
    val files: List<FilePropertyDTO>,
    val tags: List<String>,
    val isSensitive: Boolean
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


