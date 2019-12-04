package jp.panta.misskeyandroidclient.model.drive

import android.net.Uri
import java.io.File
import java.io.Serializable

class UploadFile(
    //val file: File,
    uri: Uri,
    val force: Boolean,
    var isSensitive: Boolean? = null,
    var folderId: String? = null
): Serializable{
    private val path = uri.toString()

    fun getUri(): Uri{
        return Uri.parse(path)
    }

}