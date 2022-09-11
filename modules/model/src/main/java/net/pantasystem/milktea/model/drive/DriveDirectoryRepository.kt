package net.pantasystem.milktea.model.drive

interface DriveDirectoryRepository {

    suspend fun create(createDirectory: CreateDirectory): Result<Directory>

}