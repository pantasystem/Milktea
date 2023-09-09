package net.pantasystem.milktea.model.file

import android.net.Uri
import net.pantasystem.milktea.model.UseCase

interface CopyFileToAppDirUseCase : UseCase {

    suspend operator fun invoke(uri: Uri): Result<AppFile.Local>
}