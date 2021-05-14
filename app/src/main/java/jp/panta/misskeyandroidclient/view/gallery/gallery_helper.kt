package jp.panta.misskeyandroidclient.view.gallery

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.util.ImageButtonResourceHelper.srcCompat

@BindingAdapter("galleryPostLiked", "isSendingFavorite")
fun ImageButton.setLikeButtonState(galleryPostLiked: GalleryPost?, isSendingFavorite: Boolean?) {
    val galleryPost = galleryPostLiked as? GalleryPost.Authenticated
    if(galleryPost == null || isSendingFavorite == false) {
        this.isEnabled = false
        return
    }
    this.isEnabled = true
    if(galleryPost.isLiked) {
        this.srcCompat(R.drawable.ic_baseline_red_favorite_24)
    }else{
        this.srcCompat(R.drawable.ic_baseline_favorite_border_24)
    }
}