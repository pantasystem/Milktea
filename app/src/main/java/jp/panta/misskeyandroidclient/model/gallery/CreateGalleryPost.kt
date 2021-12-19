package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.model.ITask
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.file.File

class CreateGalleryPost (
    val title: String,
    val author: Account,
    val files: List<AppFile>,
    val description: String?,
    val isSensitive: Boolean,
)


class CreateGalleryPostTask(
    val createGalleryPost: CreateGalleryPost,
    private val galleryPostRepository: GalleryRepository
) : ITask<GalleryPost> {
    override suspend fun execute(): GalleryPost {
        return galleryPostRepository.create(createGalleryPost)
    }
}

fun CreateGalleryPost.toTask(galleryPostRepository: GalleryRepository) : CreateGalleryPostTask {
    return CreateGalleryPostTask(this, galleryPostRepository)
}