package jp.panta.misskeyandroidclient.viewmodel.file

import net.pantasystem.milktea.model.file.File

interface FileListener {

    fun onSelect(file: net.pantasystem.milktea.model.file.File?)

    fun onDetach(file: net.pantasystem.milktea.model.file.File?)
}