package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User

data class GalleryPostUiState(
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
