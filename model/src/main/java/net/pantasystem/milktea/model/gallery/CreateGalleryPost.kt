package net.pantasystem.milktea.model.gallery

import net.pantasystem.milktea.model.ITask

class CreateGalleryPost (
    val title: String,
    val author: net.pantasystem.milktea.model.account.Account,
    val files: List<net.pantasystem.milktea.model.file.AppFile>,
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