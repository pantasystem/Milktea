package net.pantasystem.milktea.drive.viewmodel

import net.pantasystem.milktea.model.drive.FileProperty

data class FileViewData(
    val fileProperty: FileProperty,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val isDropdownMenuExpanded: Boolean,
)
