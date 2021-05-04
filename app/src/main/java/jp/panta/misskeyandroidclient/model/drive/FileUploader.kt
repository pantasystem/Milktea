package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.file.File

interface FileUploader {
    suspend fun upload(file: File, isForce: Boolean): FilePropertyDTO?
}