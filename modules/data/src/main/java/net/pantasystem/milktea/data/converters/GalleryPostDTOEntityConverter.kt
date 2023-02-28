package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryPostDTOEntityConverter @Inject constructor(
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
) {

    suspend fun convert(
        galleryPostDTO: net.pantasystem.milktea.api.misskey.v12_75_0.GalleryPost,
        account: Account
    ): GalleryPost {
        filePropertyDataSource.addAll(galleryPostDTO.files.map {
            filePropertyDTOEntityConverter.convert(it, account)
        })
        // NOTE: API上ではdetailだったが実際に受信されたデータはSimpleだったのでfalse
        userDataSource.add(userDTOEntityConverter.convert(account, galleryPostDTO.user, false))
        if (galleryPostDTO.likedCount == null || galleryPostDTO.isLiked == null) {
            return GalleryPost.Normal(
                GalleryPost.Id(account.accountId, galleryPostDTO.id),
                galleryPostDTO.createdAt,
                galleryPostDTO.updatedAt,
                galleryPostDTO.title,
                galleryPostDTO.description,
                User.Id(account.accountId, galleryPostDTO.userId),
                galleryPostDTO.files.map {
                    FileProperty.Id(account.accountId, it.id)
                },
                galleryPostDTO.tags ?: emptyList(),
                galleryPostDTO.isSensitive
            )
        } else {
            return GalleryPost.Authenticated(
                GalleryPost.Id(account.accountId, galleryPostDTO.id),
                galleryPostDTO.createdAt,
                galleryPostDTO.updatedAt,
                galleryPostDTO.title,
                galleryPostDTO.description,
                User.Id(account.accountId, galleryPostDTO.userId),
                galleryPostDTO.files.map {
                    FileProperty.Id(account.accountId, it.id)
                },
                galleryPostDTO.tags ?: emptyList(),
                galleryPostDTO.isSensitive,
                galleryPostDTO.likedCount ?: 0,
                galleryPostDTO.isLiked ?: false
            )
        }
    }
}