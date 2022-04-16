package net.pantasystem.milktea.data.model.gallery

import net.pantasystem.milktea.data.model.file.AppFile

class UpdateGalleryPost (
    val id: GalleryPost.Id,
    val title: String,
    val files: List<AppFile>,
    val description: String?,
    val isSensitive: Boolean,
    val tags: List<String>
)