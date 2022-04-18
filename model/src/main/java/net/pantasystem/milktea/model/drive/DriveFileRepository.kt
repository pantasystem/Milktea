package net.pantasystem.milktea.model.drive


interface DriveFileRepository {
    suspend fun find(id: FileProperty.Id) : FileProperty
    suspend fun toggleNsfw(id: FileProperty.Id)
}
