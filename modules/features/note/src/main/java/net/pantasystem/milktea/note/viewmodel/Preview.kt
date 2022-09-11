package net.pantasystem.milktea.note.viewmodel

import net.pantasystem.milktea.model.url.UrlPreview
import net.pantasystem.milktea.model.file.File
import java.io.Serializable

sealed class Preview : Serializable {

    data class UrlWrapper(
        val urlPreview: UrlPreview
    ) : Preview()

    data class FileWrapper(
        val file: File
    ) : Preview()
}