package jp.panta.misskeyandroidclient.util.svg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.apng.decode.APNGDecoder
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer





@GlideModule(glideName = "GlideApp")
class SvgModule : AppGlideModule(){

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry
            .prepend(ByteBuffer::class.java, FrameSeqDecoder::class.java, ByteBufferApngDecoder())
            .register(FrameSeqDecoder::class.java, Drawable::class.java, object : ResourceTranscoder<FrameSeqDecoder<*, *>, Drawable> {
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
            })
            .register(FrameSeqDecoder::class.java, Bitmap::class.java, object : ResourceTranscoder<FrameSeqDecoder<*, *>, Bitmap> {
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
            })
            .register(SVG::class.java, BitmapDrawable::class.java, SvgBitmapTransCoder(context))
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())

    }


    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}