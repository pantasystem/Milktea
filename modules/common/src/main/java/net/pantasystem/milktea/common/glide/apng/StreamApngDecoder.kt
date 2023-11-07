package net.pantasystem.milktea.common.glide.apng

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer

class StreamApngDecoder : ResourceDecoder<InputStream, FrameSeqDecoder<*, *>> {
    companion object {
        private const val BUFFER_SIZE = 16384
    }

    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options,
    ): Resource<FrameSeqDecoder<*, *>>? {
        val sourceBuffer = inputStreamToByteBuffer(source) ?: return null
        return ApngDecoderDelegate.decode(sourceBuffer, width, height, options)
    }

    override fun handles(source: InputStream, options: Options): Boolean {
        val sourceBuffer = inputStreamToByteBuffer(source) ?: return false
        return ApngDecoderDelegate.handles(sourceBuffer, options)
    }

    private fun inputStreamToByteBuffer(inputStream: InputStream): ByteBuffer? {
        val result = runCatching {
            ByteArrayOutputStream(BUFFER_SIZE).use {
                inputStream.copyTo(it)
                it.toByteArray()
            }
        }

        return result.getOrNull()?.let { ByteBuffer.wrap(it) }
    }
}