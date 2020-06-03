package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.UploadFile
import java.io.Serializable

class FileNoteEditorData: Serializable{
    val url: String
    val uploadFile: UploadFile?
    val fileProperty: FileProperty?
    val remoteFileId: String?
    val isLocal: Boolean

    constructor(uploadFile: UploadFile){
        this.url = uploadFile.getUri().toString()
        this.uploadFile = uploadFile
        this.fileProperty = null
        this.isLocal = true
        this.remoteFileId = null
    }
    constructor(fileProperty: FileProperty){
        this.url = fileProperty.thumbnailUrl?: fileProperty.url
        this.uploadFile = null
        this.fileProperty = fileProperty
        this.isLocal = false
        this.remoteFileId = fileProperty.id
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