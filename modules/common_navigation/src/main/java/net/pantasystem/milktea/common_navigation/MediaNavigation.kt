package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.file.FilePreviewSource

interface MediaNavigation : ActivityNavigation<MediaNavigationArgs>

sealed interface MediaNavigationArgs {
    data class Files(
        val files: List<FilePreviewSource>, val index: Int
    ) : MediaNavigationArgs

    data class AFile(
        val file: FilePreviewSource
    ) : MediaNavigationArgs
}

object MediaNavigationKeys {
    const val TAG = "MediaActivity"
    const val EXTRA_FILE = "net.pantasystem.milktea.media.MediaActivity.EXTRA_FILE"
    const val EXTRA_FILES = "net.pantasystem.milktea.media.MediaActivity.EXTRA_FILES"
    const val EXTRA_FILE_CURRENT_INDEX = "net.pantasystem.milktea.media.MediaActivity.EXTRA_FILES_CURRENT_INDEX"


}