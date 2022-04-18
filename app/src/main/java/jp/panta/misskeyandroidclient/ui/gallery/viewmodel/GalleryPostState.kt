package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData

data class GalleryPostState(
    val galleryPost: net.pantasystem.milktea.model.gallery.GalleryPost,
    val files: List<net.pantasystem.milktea.model.drive.FileProperty>,
    val user: net.pantasystem.milktea.model.user.User,
    val currentIndex: Int,
    val isFavoriteSending: Boolean,

    ) {
    val isLiked: Boolean get() = (galleryPost as? net.pantasystem.milktea.model.gallery.GalleryPost.Authenticated)?.isLiked?: false

    val fileViewDataList: List<FileViewData> by lazy {
        files.map {
            FileViewData(it.toFile())
        }
    }
}
