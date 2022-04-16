package jp.panta.misskeyandroidclient.viewmodel.file

import net.pantasystem.milktea.data.model.file.File

interface FileListener {

    fun onSelect(file: File?)

    fun onDetach(file: File?)
}