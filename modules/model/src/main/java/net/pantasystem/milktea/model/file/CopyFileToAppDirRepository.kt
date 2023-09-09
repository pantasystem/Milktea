package net.pantasystem.milktea.model.file

import android.net.Uri

interface CopyFileToAppDirRepository {

    suspend fun copyFileToAppDir(uri: Uri): Result<AppFile.Local>

    suspend fun exists(uri: Uri): Boolean

    suspend fun delete(appFile: AppFile.Local): Boolean


}