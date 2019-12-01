package jp.panta.misskeyandroidclient.viewmodel.media

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.lang.IndexOutOfBoundsException

class MediaViewData(files: List<FileProperty>) {

    val files = files.map{
        FileViewData(it)
    }

    val fileOne = this.files.getOrNull(0)
    val fileTwo = this.files.getOrNull(1)
    val fileThree = this.files.getOrNull(2)
    val fileFour = this.files.getOrNull(3)

}