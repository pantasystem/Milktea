package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.drive.FileProperty

class FileViewData(
    private val file: FileProperty
){

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


}