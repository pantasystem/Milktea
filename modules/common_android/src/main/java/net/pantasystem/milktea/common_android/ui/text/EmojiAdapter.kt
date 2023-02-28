package net.pantasystem.milktea.common_android.ui.text

import android.view.View
import java.lang.ref.WeakReference

class EmojiAdapter (view: View){

    private val weakReference = WeakReference(view)

    private  var beforeExecute: Long = System.nanoTime()

    fun update(invalidateForce: Boolean = true){
        val now = System.nanoTime()
        if ((now - beforeExecute) > 60000 || invalidateForce) {
            weakReference.get()?.invalidate()

        }
        beforeExecute = now
    }


}