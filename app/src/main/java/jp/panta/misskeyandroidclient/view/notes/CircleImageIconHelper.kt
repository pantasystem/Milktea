package jp.panta.misskeyandroidclient.view.notes

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget

object CircleImageIconHelper {

    @BindingAdapter("circleIcon")
    @JvmStatic
    fun ImageView.setCircleIcon(url: String?){
        val imageView = this
        Glide
            .with(this.context)
            .asBitmap()
            .load(url)
            .centerCrop()
            .into(object : BitmapImageViewTarget(this){
                override fun setResource(resource: Bitmap?) {
                    val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, resource)
                    circularBitmapDrawable.isCircular = true
                    imageView.setImageDrawable(circularBitmapDrawable)
                }
            })
    }
}