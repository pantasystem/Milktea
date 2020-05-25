package jp.panta.misskeyandroidclient.view.notes

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import jp.panta.misskeyandroidclient.R

object CircleImageIconHelper {

    @BindingAdapter("circleIcon")
    @JvmStatic
    fun ImageView.setCircleIcon(url: String?){

        Glide.with(this.context)
            .load(url)
            .error(R.drawable.ic_cloud_off_black_24dp)
            .apply(RequestOptions().circleCrop())
            .into(this)
    }
}