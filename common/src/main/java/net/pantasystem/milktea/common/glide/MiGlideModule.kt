package net.pantasystem.milktea.common.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import com.bumptech.glide.annotation.GlideModule
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import net.pantasystem.milktea.common.glide.apng.ByteBufferApngDecoder
import net.pantasystem.milktea.common.glide.apng.FrameSeqDecoderBitmapTranscoder
import net.pantasystem.milktea.common.glide.apng.FrameSeqDecoderDrawableTranscoder
import net.pantasystem.milktea.common.glide.svg.SvgBitmapTransCoder
import net.pantasystem.milktea.common.glide.svg.SvgDecoder
import java.io.InputStream
import java.nio.ByteBuffer





@GlideModule(glideName = "GlideApp")
class MiGlideModule : AppGlideModule(){

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry
            .prepend(ByteBuffer::class.java, FrameSeqDecoder::class.java, ByteBufferApngDecoder())
            .register(FrameSeqDecoder::class.java, Drawable::class.java, FrameSeqDecoderDrawableTranscoder())
            .register(FrameSeqDecoder::class.java, Bitmap::class.java, FrameSeqDecoderBitmapTranscoder(glide))
            .register(SVG::class.java, BitmapDrawable::class.java, SvgBitmapTransCoder(context))
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }


    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}