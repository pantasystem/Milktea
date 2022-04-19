package net.pantasystem.milktea.common.glide.svg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.caverock.androidsvg.SVG

class SvgBitmapTransCoder(val context: Context): ResourceTranscoder<SVG, BitmapDrawable>{
    override fun transcode(toTranscode: Resource<SVG>, options: Options): Resource<BitmapDrawable> {
        val svg = toTranscode.get()
        val picture: Picture = svg.renderToPicture()
        val drawable = PictureDrawable(picture)

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawPicture(drawable.picture)
        return SimpleResource(BitmapDrawable(context.resources, bitmap))
    }
}