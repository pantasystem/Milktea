package jp.panta.misskeyandroidclient.ui.drive.viewmodel.file

import net.pantasystem.milktea.model.drive.FileProperty

data class FileViewData(
    val fileProperty: net.pantasystem.milktea.model.drive.FileProperty,
    val isSelected: Boolean,
    val isEnabled: Boolean
)
