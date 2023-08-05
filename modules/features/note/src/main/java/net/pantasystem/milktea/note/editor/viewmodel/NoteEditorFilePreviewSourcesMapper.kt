package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource

class NoteEditorFilePreviewSourcesMapper(
    private val filePropertyDataSource: FilePropertyDataSource,
    private val driveFileRepository: DriveFileRepository,
    private val logger: Logger,
    private val viewModelScope: CoroutineScope,
) {

    fun create(filesFlow: StateFlow<List<AppFile>>): StateFlow<List<FilePreviewSource>> {
        @OptIn(ExperimentalCoroutinesApi::class)
        val driveFilesFlow = filesFlow.flatMapLatest { files ->
            val fileIds = files.mapNotNull {
                it as? AppFile.Remote
            }.map {
                it.id
            }
            filePropertyDataSource.observeIn(fileIds)
        }.catch {
            logger.error("drive fileの取得に失敗", it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        return combine(filesFlow, driveFilesFlow) { files, driveFiles ->
            files.mapNotNull { appFile ->
                when (appFile) {
                    is AppFile.Local -> {
                        FilePreviewSource.Local(appFile)
                    }

                    is AppFile.Remote -> {
                        runCancellableCatching {
                            driveFiles.firstOrNull {
                                it.id == appFile.id
                            } ?: driveFileRepository.find(appFile.id)
                        }.getOrNull()?.let {
                            FilePreviewSource.Remote(appFile, it)
                        }
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

}