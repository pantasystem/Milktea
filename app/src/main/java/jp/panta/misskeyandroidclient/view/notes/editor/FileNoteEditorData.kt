package jp.panta.misskeyandroidclient.view.notes.editor

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.io.File

class FileNoteEditorData{
    val url: String
    val file: File?
    val fileProperty: FileProperty?
    val isLocal: Boolean
    constructor(file: File){
        this.url = file.path
        this.file = file
        this.fileProperty = null
        this.isLocal = true
    }
    constructor(fileProperty: FileProperty){
        this.url = fileProperty.thumbnailUrl?: fileProperty.url.toString()
        this.file = null
        this.fileProperty = fileProperty
        this.isLocal = false
    }

    override fun toString(): String {
        return "FileNoteEditorData(url='$url', file=$file, fileProperty=$fileProperty, isLocal=$isLocal)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileNoteEditorData

        if (url != other.url) return false
        if (file != other.file) return false
        if (fileProperty != other.fileProperty) return false
        if (isLocal != other.isLocal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (file?.hashCode() ?: 0)
        result = 31 * result + (fileProperty?.hashCode() ?: 0)
        result = 31 * result + isLocal.hashCode()
        return result
    }
}