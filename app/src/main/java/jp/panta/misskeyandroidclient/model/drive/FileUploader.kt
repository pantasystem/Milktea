package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.model.file.File

interface FileUploader {
    fun upload(file: File, isForce: Boolean): FileProperty?
}