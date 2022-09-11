package net.pantasystem.milktea.drive.viewmodel

import net.pantasystem.milktea.model.drive.Directory

class PathViewData (val folder: Directory?){
    val id = folder?.id
    val name = folder?.name?: "root"
    val parentId = folder?.parentId
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathViewData

        if (folder != other.folder) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = folder?.hashCode() ?: 0
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Directory(folder=$folder, id=$id, name='$name', parentId=$parentId)"
    }

}