package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.url.UrlPreview
import java.io.Serializable

sealed class Preview : Serializable {

    data class UrlWrapper(
        val urlPreview: UrlPreview
    ) : Preview()

    data class FileWrapper(
        val file: File
    ) : Preview()
}