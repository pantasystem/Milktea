package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.io.Serializable

class FileViewData(
    val file: FileProperty
): Serializable{

    val id: String = file.id
    val name = file.name
    val type = file.type
    val md5 = file.md5
    val size = file.size
    val userId = file.userId
    val comment = file.comment
    val isSensitive = file.isSensitive
    val url = file.url
    val thumbnailUrl = file.thumbnailUrl
    val attachedNoteIds = file.attachedNoteIds
    val folderId = file.folderId


    //FileViewModelから制御する
    val isSelect = MutableLiveData<Boolean>(false)
    val isEnabledSelect = MutableLiveData<Boolean>(true)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileViewData

        if (file != other.file) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (md5 != other.md5) return false
        if (size != other.size) return false
        if (userId != other.userId) return false
        if (comment != other.comment) return false
        if (isSensitive != other.isSensitive) return false
        if (url != other.url) return false
        if (thumbnailUrl != other.thumbnailUrl) return false
        if (attachedNoteIds != other.attachedNoteIds) return false
        if (folderId != other.folderId) return false
        if (isSelect != other.isSelect) return false
        if (isEnabledSelect != other.isEnabledSelect) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (md5?.hashCode() ?: 0)
        result = 31 * result + (size ?: 0)
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (isSensitive?.hashCode() ?: 0)
        result = 31 * result + (url.hashCode())
        result = 31 * result + (thumbnailUrl?.hashCode() ?: 0)
        result = 31 * result + (attachedNoteIds?.hashCode() ?: 0)
        result = 31 * result + (folderId?.hashCode() ?: 0)
        result = 31 * result + isSelect.hashCode()
        result = 31 * result + isEnabledSelect.hashCode()
        return result
    }


}