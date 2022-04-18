package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData

data class GalleryPostState(
    val galleryPost: GalleryPost,
    val files: List<FileProperty>,
    val user: User,
    val currentIndex: Int,
    val isFavoriteSending: Boolean,

    ) {
    val isLiked: Boolean get() = (galleryPost as? GalleryPost.Authenticated)?.isLiked?: false

    val fileViewDataList: List<FileViewData> by lazy {
        files.map {
            FileViewData(it.toFile())
        }
    }
}
