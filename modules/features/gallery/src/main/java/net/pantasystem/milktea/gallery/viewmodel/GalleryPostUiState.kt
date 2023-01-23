package net.pantasystem.milktea.gallery.viewmodel

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User

data class GalleryPostUiState(
    val galleryPost: GalleryPost,
    val files: List<FileProperty>,
    val user: User,
    val currentIndex: Int,
    val isFavoriteSending: Boolean
)
