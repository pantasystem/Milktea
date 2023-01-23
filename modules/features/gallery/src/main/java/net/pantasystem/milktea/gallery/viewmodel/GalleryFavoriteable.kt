package net.pantasystem.milktea.gallery.viewmodel

import net.pantasystem.milktea.model.gallery.GalleryPost

interface GalleryToggleLikeOrUnlike  {

    suspend fun toggle(galleryId: GalleryPost.Id)
}