package net.pantasystem.milktea.model.drive

interface DriveDirectoryRepository {

    suspend fun create(createDirectory: CreateDirectory): Result<Directory>

    /**
     * ディレクトリの取得を行う
     */
    suspend fun findOne(id: DirectoryId): Result<Directory>


}