package net.pantasystem.milktea.model.gallery

import net.pantasystem.milktea.model.ITask
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.file.AppFile

data class CreateGalleryPost (
    val title: String,
    val author: Account,
    val files: List<AppFile>,
    val description: String?,
    val isSensitive: Boolean,
)


class CreateGalleryPostTask(
    private val createGalleryPost: CreateGalleryPost,
    private val galleryPostRepository: GalleryRepository
) : ITask<GalleryPost> {
    override suspend fun execute(): GalleryPost {
        return galleryPostRepository.create(createGalleryPost)
    }
}

fun CreateGalleryPost.toTask(galleryPostRepository: GalleryRepository) : CreateGalleryPostTask {
    return CreateGalleryPostTask(this, galleryPostRepository)
}