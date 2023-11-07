package net.pantasystem.milktea.model.file


class FileUploadFailedException(
    val file: AppFile,
    val throwable: Throwable?,
    statusCode: Int?,
    errorMessage: String?,
) : IllegalStateException(
    "ファイルアップロードに失敗: file:$file, statusCode:$statusCode, message:$errorMessage",
    throwable
)