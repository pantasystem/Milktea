package jp.panta.misskeyandroidclient.util.svg

import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.apng.decode.APNGDecoder
import com.github.penfeizhou.animation.apng.decode.APNGParser
import com.github.penfeizhou.animation.apng.io.APNGReader
import com.github.penfeizhou.animation.io.ByteBufferReader
import com.github.penfeizhou.animation.io.StreamReader
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import java.nio.ByteBuffer

import com.github.penfeizhou.animation.loader.ByteBufferLoader
import com.github.penfeizhou.animation.loader.Loader
import okhttp3.internal.toHexString
import com.github.penfeizhou.animation.decode.FrameSeqDecoder





const val PNG: Long = 0x89504E47

//class ApngDecoder : ResourceDecoder<InputStream, APNGDrawable> {
//
//    override fun decode(
//        source: InputStream,
//        width: Int,
//        height: Int,
//        options: Options
//    ): Resource<APNGDrawable> {
//
//        Log.d("ApngDecoder", "apng decoder on decode")
//        return APNGDecoder()
//    }
//
//    override fun handles(source: InputStream, options: Options): Boolean {
//        val byteBuffer = ByteArray(8)
//        val len = source.read(byteBuffer)
//        if (len < 8) {
//            Log.d("ApngDecoder", "pngではない")
//            return false
//        }
//        val header = ByteBuffer.wrap(byteBuffer).long
//        if(header != PNG) {
//            Log.d("ApngDecoder", "pngではない")
//            return false
//        }
//        Log.d("ApngDecoder", "png確定")
//
//        val result = APNGParser.isAPNG(APNGReader(StreamReader(source)))
//        Log.d("ApngDecoder", "handlers isApng:$result")
//        return result
//    }
//}

class ByteBufferApngDecoder : ResourceDecoder<ByteBuffer, FrameSeqDecoder<*, *>> {


    override fun decode(
        source: ByteBuffer,
        width: Int,
        height: Int,
        options: Options
    ): Resource<FrameSeqDecoder<*, *>> {
        val loader: Loader = object : ByteBufferLoader() {
            override fun getByteBuffer(): ByteBuffer {
                source.position(0)
                return source
            }
        }
        val decoder = APNGDecoder(loader, null)
        return FrameSeqDecoderResource(decoder, source.limit())
    }

    override fun handles(source: ByteBuffer, options: Options): Boolean {
        Log.d("ByteBufferApngDecoder", "apng decoder on decode")
        val byteBufferArray = ByteArray(8)
        source.get(byteBufferArray, 0, 4)
        val header = ByteBuffer.wrap(byteBufferArray).long ushr 32
        if (header != PNG) {
            Log.d("ByteBufferApngDecoder", "is not png:${header.toHexString()}")
            return false
        }

        val result = APNGParser.isAPNG(ByteBufferReader(source))
        Log.d("ByteBufferApngDecoder", "handlers isApng:$result")
        return result

    }

    private class FrameSeqDecoderResource(
        private val decoder: FrameSeqDecoder<*, *>,
        private val size: Int
    ) : Resource<FrameSeqDecoder<*, *>> {
        override fun getResourceClass(): Class<FrameSeqDecoder<*, *>> {
            return FrameSeqDecoder::class.java
        }

        override fun get(): FrameSeqDecoder<*, *> {
            return decoder
        }

        override fun getSize(): Int {
            return size
        }

        override fun recycle() {
            decoder.stop()
        }
    }

}
