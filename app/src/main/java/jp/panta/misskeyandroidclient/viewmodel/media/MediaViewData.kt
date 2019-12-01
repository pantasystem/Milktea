package jp.panta.misskeyandroidclient.viewmodel.media

import jp.panta.misskeyandroidclient.model.drive.FileProperty

class MediaViewData(files: List<FileProperty>) {

    val files = files.map{
        FileViewData(it)
    }

    fun changeHidingState(index: Int){
        if(index < files.size){
            files[index].changeContentHiding()
        }
    }
}