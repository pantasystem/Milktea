package jp.panta.misskeyandroidclient.model.drive

interface FileUploader {
    fun upload(uploadFile: UploadFile): FileProperty?
}