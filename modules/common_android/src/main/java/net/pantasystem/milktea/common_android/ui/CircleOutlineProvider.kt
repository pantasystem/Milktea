package net.pantasystem.milktea.common_android.ui

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import java.util.concurrent.ConcurrentHashMap

class CircleOutlineProvider(val rect: Float) : ViewOutlineProvider(){

    companion object{

        private val rectMap = ConcurrentHashMap<Float, CircleOutlineProvider>()

        fun getInstance(rect: Float): CircleOutlineProvider {
            var provider = rectMap[rect]
            if(provider == null){
                provider = CircleOutlineProvider(rect)
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