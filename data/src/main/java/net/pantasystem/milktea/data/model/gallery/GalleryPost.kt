package net.pantasystem.milktea.data.model.gallery


import net.pantasystem.milktea.data.api.misskey.users.toUser
import net.pantasystem.milktea.data.model.EntityId
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.drive.FileProperty
import net.pantasystem.milktea.data.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.data.model.users.User
import net.pantasystem.milktea.data.model.users.UserDataSource
import java.util.*
import net.pantasystem.milktea.data.api.misskey.v12_75_0.GalleryPost as GalleryPostDTO

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
    abstract val userId: User.Id
    abstract val fileIds: List<FileProperty.Id>
    abstract val tags: List<String>
    abstract val isSensitive: Boolean

    data class Normal(
        override val id: Id,
        override val createdAt: Date,
        override val updatedAt: Date,
        override val title: String,
        override val description: String?,
        override val userId: User.Id,
        override val fileIds: List<FileProperty.Id>,
        override val tags: List<String>,
        override val isSensitive: Boolean
    ) : GalleryPost()

    data class Authenticated(
        override val id: Id,
        override val createdAt: Date,
        override val updatedAt: Date,
        override val title: String,
        override val description: String?,
        override val userId: User.Id,
        override val fileIds: List<FileProperty.Id>,
        override val tags: List<String>,
        override val isSensitive: Boolean,
        val likedCount: Int,
        val isLiked: Boolean
    ) : GalleryPost()
}

suspend fun GalleryPostDTO.toEntity(
    account: Account,
    filePropertyDataSource: FilePropertyDataSource,
    userDataSource: UserDataSource
): GalleryPost {
    filePropertyDataSource.addAll(files.map {
        it.toFileProperty(account)
    })
    // NOTE: API上ではdetailだったが実際に受信されたデータはSimpleだったのでfalse
    userDataSource.add(user.toUser(account, false))
    if (this.likedCount == null || this.isLiked == null) {

        return GalleryPost.Normal(
            GalleryPost.Id(account.accountId, this.id),
            createdAt,
            updatedAt,
            title,
            description,
            User.Id(account.accountId, userId),
            files.map {
                FileProperty.Id(account.accountId, it.id)
            },
            tags ?: emptyList(),
            isSensitive
        )
    } else {
        return GalleryPost.Authenticated(
            GalleryPost.Id(account.accountId, this.id),
            createdAt,
            updatedAt,
            title,
            description,
            User.Id(account.accountId, userId),
            files.map {
                FileProperty.Id(account.accountId, it.id)
            },
            tags ?: emptyList(),
            isSensitive,
            likedCount,
            isLiked
        )
    }
}


data class GalleryPostRelation(
    val galleryPost: GalleryPost,
    val files: List<FileProperty>,
    val user: User,
)