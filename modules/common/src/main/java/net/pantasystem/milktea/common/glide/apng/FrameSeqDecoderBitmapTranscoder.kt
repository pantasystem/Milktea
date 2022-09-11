package net.pantasystem.milktea.common.glide.apng

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import java.io.IOException

class FrameSeqDecoderBitmapTranscoder(
    val glide: Glide
) : ResourceTranscoder<FrameSeqDecoder<*, *>, Bitmap> {
    override fun transcode(
        toTranscode: Resource<FrameSeqDecoder<*, *>>,
        options: Options
    ): Resource<Bitmap>? {
        val frame = toTranscode.get()
        try {
            val bitmap = frame.getFrameBitmap(0)
            return BitmapResource.obtain(bitmap, glide.bitmapPool)
        } catch(e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}