package net.pantasystem.milktea.data.infrastructure.file

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Hash
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.CopyFileToAppDirUseCase
import net.pantasystem.milktea.model.file.toAppFile
import java.io.File
import javax.inject.Inject

class CopyFileToAppDirUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): CopyFileToAppDirUseCase {

    companion object {
        const val USER_FILE_DIR = "user_files"
    }
    override suspend fun invoke(uri: Uri): Result<AppFile.Local> = runCancellableCatching {
        withContext(ioDispatcher) {
            val localFile = uri.toAppFile(context)
            val appDir = context.filesDir ?: throw IllegalStateException("アプリディレクトリの取得に失敗")
            val pathHash = Hash.sha256(localFile.path)
            val copyTo = File(appDir, USER_FILE_DIR).apply {
                if (!exists()) {
                    mkdirs()
                }
            }.resolve(pathHash)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                copyTo.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            localFile.copy(path = copyTo.path)
        }
    }
}

