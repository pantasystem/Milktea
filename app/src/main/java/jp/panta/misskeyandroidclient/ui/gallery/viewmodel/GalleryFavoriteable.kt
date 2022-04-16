package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import net.pantasystem.milktea.data.model.gallery.GalleryPost

interface GalleryToggleLikeOrUnlike  {

    suspend fun toggle(galleryId: GalleryPost.Id)
}