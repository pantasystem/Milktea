package net.pantasystem.milktea.common.glide.apng

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.apng.decode.APNGDecoder
import com.github.penfeizhou.animation.decode.FrameSeqDecoder

class FrameSeqDecoderDrawableTranscoder : ResourceTranscoder<FrameSeqDecoder<*, *>, Drawable> {
    override fun transcode(
        toTranscode: Resource<FrameSeqDecoder<*, *>>,
        options: Options
    ): Resource<Drawable> {
        val apngDrawable = APNGDrawable(toTranscode.get() as APNGDecoder)
        apngDrawable.setAutoPlay(false)
        return object : DrawableResource<Drawable>(apngDrawable) {
            override fun getResourceClass(): Class<Drawable> {
                return Drawable::class.java
            }

            override fun getSize(): Int {
                return apngDrawable.memorySize
            }

            override fun recycle() {
                apngDrawable.stop()
            }

        }
    }
}