package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.drive.FileProperty

const val EXTRA_INT_SELECTABLE_FILE_MAX_SIZE =
    "jp.panta.misskeyandroidclient.EXTRA_INT_SELECTABLE_FILE_SIZE"
const val EXTRA_SELECTED_FILE_PROPERTY_IDS =
    "jp.panta.misskeyandroiclient.EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID"
const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"

interface DriveNavigation : ActivityNavigation<DriveNavigationArgs>


data class DriveNavigationArgs(
    val selectableFileMaxSize: Int? = null,
    val selectedFilePropertyIds: List<FileProperty.Id>? = null,
    val accountId: Long? = null,
)