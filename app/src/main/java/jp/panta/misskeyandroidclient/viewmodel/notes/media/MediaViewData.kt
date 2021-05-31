package jp.panta.misskeyandroidclient.viewmodel.notes.media

import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData

class MediaViewData(files: List<File>) {

    val files = files.map{
        FileViewData(it)
    }

    val fileOne = this.files.getOrNull(0)
    val fileTwo = this.files.getOrNull(1)
    val fileThree = this.files.getOrNull(2)
    val fileFour = this.files.getOrNull(3)

}