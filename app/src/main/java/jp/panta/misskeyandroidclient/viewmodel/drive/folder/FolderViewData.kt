package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import jp.panta.misskeyandroidclient.model.drive.FolderProperty

class FolderViewData (private val folderProperty: FolderProperty?){
    //nullの場合はroot
    val id = folderProperty?.id

}