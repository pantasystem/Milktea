package net.pantasystem.milktea.common.glide.svg

import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.request.target.Target
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.InputStream
import java.nio.ByteBuffer

private const val SVG_HEADER: Int = 0x3C737667
private const val SVG_HEADER_STARTS_WITH_XML = 0x3c3f786d
class SvgDecoder : ResourceDecoder<InputStream, SVG>{

    companion object {
        fun isSvg(source: InputStream): Boolean {
            val buffer = ByteArray(8)
            val cnt = source.read(buffer)
            if (cnt < 8) {
                Log.d("SvgDecoder", "svgではない")
                return false
            }

            val header = ByteBuffer.wrap(buffer).int
            return header == SVG_HEADER || header == SVG_HEADER_STARTS_WITH_XML
        }
    }

    override fun handles(source: InputStream, options: Options): Boolean {
        return isSvg(source)
    }

    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options
    ): Resource<SVG>? {
        return try {
            val svg = SVG.getFromInputStream(source)
            if (width != Target.SIZE_ORIGINAL) {
                svg.documentWidth = width.toFloat()
            }
            if (height != Target.SIZE_ORIGINAL) {
                svg.documentHeight = height.toFloat()
            }
            Log.d("SvgDecoder", "width: $width, height: $height")
            SimpleResource(svg)
        }catch(e: SVGParseException){
            Log.e("SvgDecoder", "error", e)
            null
        }

    }
}