package jp.panta.misskeyandroidclient.viewmodel.file

import jp.panta.misskeyandroidclient.model.file.File

interface FileListener {

    fun onSelect(file: File)

    fun onDetach(file: File)
}