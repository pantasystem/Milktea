package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import net.pantasystem.milktea.model.gallery.GalleryPost

interface GalleryToggleLikeOrUnlike  {

    suspend fun toggle(galleryId: net.pantasystem.milktea.model.gallery.GalleryPost.Id)
}