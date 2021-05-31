package jp.panta.misskeyandroidclient.viewmodel.gallery

import jp.panta.misskeyandroidclient.model.gallery.GalleryPost

interface GalleryToggleLikeOrUnlike  {

    suspend fun toggle(galleryId: GalleryPost.Id)
}