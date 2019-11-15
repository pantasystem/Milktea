package jp.panta.misskeyandroidclient.model.drive

import java.io.File

data class UploadFile(
    val file: File,
    val force: Boolean,
    var isSensitive: Boolean? = null,
    var folderId: String? = null
)