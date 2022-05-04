package net.pantasystem.milktea.model.drive

import net.pantasystem.milktea.model.file.AppFile


interface DriveFileRepository {
    suspend fun find(id: FileProperty.Id) : FileProperty
    suspend fun toggleNsfw(id: FileProperty.Id)
    suspend fun create(accountId:Long, file: AppFile.Local): Result<FileProperty>
    suspend fun delete(id: FileProperty.Id): Result<Unit>
    suspend fun update(updateFileProperty: UpdateFileProperty): Result<FileProperty>

}
