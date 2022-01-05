package jp.panta.misskeyandroidclient.util

import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.BindingAdapter

object ImageButtonResourceHelper {
    @BindingAdapter("srcCompat")
    @JvmStatic
    fun ImageButton.srcCompat(resourceId: Int?){
        if(resourceId != null){
            this.setImageResource(resourceId)
        }
    }

    @BindingAdapter("srcCompat")
    @JvmStatic
    fun ImageView.srcCompat(resourceId: Int?){
        if(resourceId != null){
            this.setImageResource(resourceId)
        }
    }


}