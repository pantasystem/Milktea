package jp.panta.misskeyandroidclient.view.notes.editor

import android.net.Uri
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.UploadFile
import jp.panta.misskeyandroidclient.model.notes.draft.DraftFile
import java.io.File
import java.io.Serializable

class FileNoteEditorData: Serializable{
    val url: String
    val uploadFile: UploadFile?
    val fileProperty: FileProperty?
    val isLocal: Boolean
    constructor(uploadFile: UploadFile){
        this.url = uploadFile.getUri().toString()
        this.uploadFile = uploadFile
        this.fileProperty = null
        this.isLocal = true
    }
    constructor(fileProperty: FileProperty){
        this.url = fileProperty.thumbnailUrl?: fileProperty.url
        this.uploadFile = null
        this.fileProperty = fileProperty
        this.isLocal = false
    }

    constructor(draftFile: DraftFile){
        this.url = draftFile.filePath?: ""
        this.uploadFile = UploadFile(Uri.parse(this.url), true)
        this.fileProperty = null
        this.isLocal = draftFile.remoteFileId == null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileNoteEditorData

        if (url != other.url) return false
        if (uploadFile != other.uploadFile) return false
        if (fileProperty != other.fileProperty) return false
        if (isLocal != other.isLocal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (uploadFile?.hashCode() ?: 0)
        result = 31 * result + (fileProperty?.hashCode() ?: 0)
        result = 31 * result + isLocal.hashCode()
        return result
    }

    override fun toString(): String {
        return "FileNoteEditorData(url='$url', uploadFile=$uploadFile, fileProperty=$fileProperty, isLocal=$isLocal)"
    }


}