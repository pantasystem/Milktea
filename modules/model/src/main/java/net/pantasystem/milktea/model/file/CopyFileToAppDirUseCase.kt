package net.pantasystem.milktea.model.file

import android.net.Uri
import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject

class CopyFileToAppDirUseCase @Inject constructor(
    private val repository: CopyFileToAppDirRepository
): UseCase {

    suspend operator fun invoke(uri: Uri): Result<AppFile.Local> {
        return repository.copyFileToAppDir(uri)
    }
}