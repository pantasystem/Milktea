package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.io.Serializable

data class FileViewData(
    val fileProperty: FileProperty,
    val isSelected: Boolean,
)
