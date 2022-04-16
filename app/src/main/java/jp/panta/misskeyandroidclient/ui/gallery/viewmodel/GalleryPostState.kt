package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import net.pantasystem.milktea.data.model.drive.FileProperty
import net.pantasystem.milktea.data.model.gallery.GalleryPost
import net.pantasystem.milktea.data.model.users.User
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
