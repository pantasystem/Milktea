package net.pantasystem.milktea.common.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import net.pantasystem.milktea.common.R

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