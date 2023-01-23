package net.pantasystem.milktea.common.glide.apng

import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.github.penfeizhou.animation.apng.decode.APNGDecoder
import com.github.penfeizhou.animation.apng.decode.APNGParser
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.io.ByteBufferReader
import com.github.penfeizhou.animation.loader.ByteBufferLoader
import com.github.penfeizhou.animation.loader.Loader
import okhttp3.internal.toHexString
import java.nio.ByteBuffer


const val PNG: Long = 0x89504E47


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
