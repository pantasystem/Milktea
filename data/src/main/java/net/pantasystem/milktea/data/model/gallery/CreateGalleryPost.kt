package net.pantasystem.milktea.data.model.gallery

import net.pantasystem.milktea.data.model.ITask
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.file.AppFile
import net.pantasystem.milktea.data.model.file.File

class CreateGalleryPost (
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