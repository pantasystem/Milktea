package jp.panta.misskeyandroidclient.viewmodel.drive

import jp.panta.misskeyandroidclient.model.drive.FolderProperty

class Directory (val folder: FolderProperty?){
    val id = folder?.id
    val name = folder?.name?: "root"
    val parentId = folder?.parentId

}