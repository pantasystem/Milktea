package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import jp.panta.misskeyandroidclient.model.drive.Directory

class FolderViewData (val directory: Directory){
    val id = directory.id
    val createdAt = directory.createdAt
    val name = directory.name
    val foldersCount = directory.foldersCount
    val filesCount = directory.filesCount
    val parentId = directory.parentId
    val parent = directory.parent
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderViewData

        if (directory != other.directory) return false
        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (name != other.name) return false
        if (foldersCount != other.foldersCount) return false
        if (filesCount != other.filesCount) return false
        if (parentId != other.parentId) return false
        if (parent != other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = directory.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + foldersCount
        result = 31 * result + filesCount
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + (parent?.hashCode() ?: 0)
        return result
    }

}