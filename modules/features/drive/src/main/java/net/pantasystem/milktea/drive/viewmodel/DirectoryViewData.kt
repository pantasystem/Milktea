package net.pantasystem.milktea.drive.viewmodel

import net.pantasystem.milktea.model.drive.Directory

data class DirectoryViewData (val directory: Directory){
    val id = directory.id
    val createdAt = directory.createdAt
    val name = directory.name
    val parent = directory.parent


}