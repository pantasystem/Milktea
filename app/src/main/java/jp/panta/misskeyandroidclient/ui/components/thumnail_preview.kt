package jp.panta.misskeyandroidclient.ui.components

import androidx.compose.runtime.Composable
import jp.panta.misskeyandroidclient.model.drive.FileProperty

@Composable
fun ThumbnailPreview(file: FileProperty, onClick: ()->Unit) {
    when {
        file.type.startsWith("image") -> {

        }
        file.type.startsWith("video") -> {

        }
        else -> throw IllegalArgumentException("サポートしていないファイルタイプです")
    }
}