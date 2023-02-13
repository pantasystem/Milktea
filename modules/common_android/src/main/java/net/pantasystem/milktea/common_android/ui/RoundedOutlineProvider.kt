package net.pantasystem.milktea.common_android.ui

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import java.util.concurrent.ConcurrentHashMap

class RoundedOutlineProvider(val rect: Float) : ViewOutlineProvider(){

    companion object{

        private val rectMap = ConcurrentHashMap<Float, RoundedOutlineProvider>()

        fun getInstance(rect: Float): RoundedOutlineProvider {
            var provider = rectMap[rect]
            if(provider == null){
                provider = RoundedOutlineProvider(rect)
                rectMap[rect] = provider
            }
            return provider
        }

    }

    override fun getOutline(view: View?, outline: Outline?) {
        view?: return
        outline?: return
        outline.setRoundRect(0, 0, view.width, view.height, rect)
        view.clipToOutline = true
    }

}