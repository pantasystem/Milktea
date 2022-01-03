package jp.panta.misskeyandroidclient.view.gallery

import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.util.ImageButtonResourceHelper.srcCompat
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostState

@BindingAdapter("isLiked", "isSendingLiked")
fun ImageButton.setLikeButtonState(isLiked: Boolean?, isSendingFavorite: Boolean?) {
    if(isLiked == null || isSendingFavorite == true) {
        this.isEnabled = false
    }
    this.isEnabled = true
    if(isLiked == true) {
        this.srcCompat(R.drawable.ic_baseline_red_favorite_24)
    }else{
        this.srcCompat(R.drawable.ic_baseline_favorite_border_24)
    }
}

