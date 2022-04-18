package net.pantasystem.milktea.model.gallery

class UpdateGalleryPost (
    val id: GalleryPost.Id,
    val title: String,
    val files: List<net.pantasystem.milktea.model.file.AppFile>,
    val description: String?,
    val isSensitive: Boolean,
    val tags: List<String>
)