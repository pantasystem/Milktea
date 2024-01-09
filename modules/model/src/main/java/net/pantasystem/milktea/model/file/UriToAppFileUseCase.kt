package net.pantasystem.milktea.model.file

import android.net.Uri

interface UriToAppFileUseCase {
    operator fun invoke(uri: Uri): AppFile.Local
}
