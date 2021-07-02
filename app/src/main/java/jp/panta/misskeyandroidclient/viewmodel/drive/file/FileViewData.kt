package jp.panta.misskeyandroidclient.viewmodel.drive.file

import jp.panta.misskeyandroidclient.model.drive.FileProperty

data class FileViewData(
    val fileProperty: FileProperty,
    val isSelected: Boolean,
    val isEnabled: Boolean
)
