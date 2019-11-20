package jp.panta.misskeyandroidclient.view.settings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ImageViewHelper{

    @JvmStatic
    @BindingAdapter("drawableResourceId")
    fun ImageView.setImageFromResource(drawableResourceId: Int){
        Glide.with(this)
            .load(drawableResourceId)
            .centerCrop()
            .into(this)
    }
}