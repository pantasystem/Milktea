package jp.panta.misskeyandroidclient.view.notes

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget

object CircleImageIconHelper {

    @BindingAdapter("circleIcon")
    @JvmStatic
    fun ImageView.setCircleIcon(url: String?){
        val imageView = this
        Glide
            .with(this.context)
            .load(url)
            .asBitmap()
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