package net.pantasystem.milktea.common.glide.apng

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.github.penfeizhou.animation.apng.decode.APNGParser
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.io.StreamReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class StreamApngDecoder(
    val byteBufferApngDecoder: ByteBufferApngDecoder,
) : ResourceDecoder<InputStream, FrameSeqDecoder<*, *>> {
    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options,
    ): Resource<FrameSeqDecoder<*, *>>? {
        val data = inputStreamToBytes(source) ?: return null
        val byteBuffer = ByteBuffer.wrap(data)
        return byteBufferApngDecoder.decode(byteBuffer, width, height, options)
    }

    override fun handles(source: InputStream, options: Options): Boolean {
        val headerBytes = ByteArray(8)
        val bytesRead = source.read(headerBytes)
        // ファイルが8バイト未満の場合、それは有効なPNGではない
        if (bytesRead < 8) {
            return false
        }

        val header = ByteBuffer.wrap(headerBytes).long ushr 32
        if (header != PNG) {
            return false
        }
        return APNGParser.isAPNG(StreamReader(source))
    }

    private fun inputStreamToBytes(`is`: InputStream): ByteArray? {
        val bufferSize = 16384
        val buffer = ByteArrayOutputStream(bufferSize)
        try {
            var nRead: Int
            val data = ByteArray(bufferSize)
            while (`is`.read(data).also { nRead = it } != -1) {
                buffer.write(data, 0, nRead)
            }
            buffer.flush()
        } catch (e: IOException) {
            return null
        }
        return buffer.toByteArray()
    }
}