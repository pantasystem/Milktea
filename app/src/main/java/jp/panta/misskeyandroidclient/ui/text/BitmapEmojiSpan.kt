package jp.panta.misskeyandroidclient.ui.text

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class BitmapEmojiSpan(adapter: EmojiAdapter) : EmojiSpan<Bitmap>(adapter){





    override val target = object : CustomTarget<Bitmap>(){
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            imageDrawable = BitmapDrawable(adapter.weakReference.get()?.context?.resources, resource)
            adapter.throttleUpdate()
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }
}