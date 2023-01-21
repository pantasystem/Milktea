package net.pantasystem.milktea.data.infrastructure.drive

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.InputStream

internal class InputStreamRequestBody(
    val type: String,
    val inputStream: InputStream,
) : RequestBody() {
    override fun contentType(): MediaType {
        return type.toMediaType()
    }

    override fun writeTo(sink: BufferedSink) {
        inputStream.source().use {
            sink.writeAll(it)
        }
    }
}