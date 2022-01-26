package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import jp.panta.misskeyandroidclient.model.gallery.GalleryPost

interface GalleryToggleLikeOrUnlike  {

    suspend fun toggle(galleryId: GalleryPost.Id)
}