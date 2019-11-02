package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import jp.panta.misskeyandroidclient.model.drive.FolderProperty

class FolderViewData (val folderProperty: FolderProperty){
    val id = folderProperty.id
    val createdAt = folderProperty.createdAt
    val name = folderProperty.name
    val foldersCount = folderProperty.foldersCount
    val filesCount = folderProperty.filesCount
    val parentId = folderProperty.parentId
    val parent = folderProperty.parent

}