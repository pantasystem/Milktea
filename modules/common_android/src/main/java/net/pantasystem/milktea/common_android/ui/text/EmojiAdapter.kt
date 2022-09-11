package net.pantasystem.milktea.common_android.ui.text

import android.view.View
import java.lang.ref.WeakReference

class EmojiAdapter (view: View){

    private val weakReference = WeakReference(view)



    fun update(){
        weakReference.get()?.invalidate()
    }


}