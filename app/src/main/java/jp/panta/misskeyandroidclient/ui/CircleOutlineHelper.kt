package jp.panta.misskeyandroidclient.ui

import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.util.CircleOutlineProvider

object CircleOutlineHelper {
    @JvmStatic
    @BindingAdapter("rect")
    fun ViewGroup.setCircleOutline(rect: Float?){

        outlineProvider = CircleOutlineProvider.getInstance(rect?: 20F)
    }

    @JvmStatic
    @BindingAdapter("reactByDp")
    fun ViewGroup.setCircleOutline(rect: Int?){
        rect?: return
        val r = context.resources.displayMetrics.density * rect

        outlineProvider = CircleOutlineProvider.getInstance(r)
    }
}