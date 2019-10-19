package jp.panta.misskeyandroidclient.util.svg

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.annotation.GlideModule
import java.io.InputStream

@GlideModule(glideName = "GlideApp")
class SvgMoudule : AppGlideModule(){

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry
            .register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }


    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}