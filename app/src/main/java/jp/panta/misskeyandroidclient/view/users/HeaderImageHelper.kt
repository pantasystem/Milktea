package jp.panta.misskeyandroidclient.view.users

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object HeaderImageHelper {

    @BindingAdapter("headerImageUrl")
    @JvmStatic
    fun ImageView.putImage(headerImageUrl: String?){
        if(headerImageUrl == null){
            return
        }else{
            Glide.with(this)
                .load(headerImageUrl)
                .centerCrop()
                .into(this)
        }
    }
}