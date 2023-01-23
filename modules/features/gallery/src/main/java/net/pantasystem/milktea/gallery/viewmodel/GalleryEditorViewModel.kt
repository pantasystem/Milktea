package net.pantasystem.milktea.gallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.CreateGalleryTaskExecutor
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.gallery.CreateGalleryPost
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.gallery.GalleryRepository
import net.pantasystem.milktea.model.gallery.toTask
import java.io.Serializable
import javax.inject.Inject


sealed class EditType : Serializable {
    data class Update(
        val postId: GalleryPost.Id
    ) : EditType()

    data class Create(
        val accountId: Long?
    ) : EditType()
}

data class GalleryEditorUiState(
    val type: EditType,
    val title: String = "",
    val description: String? = null,
    val pickedImages: List<AppFile> = emptyList(),
    val isSensitive: Boolean = false,
) {

    fun validate(): Boolean {
        return this.pickedImages.isNotEmpty() && this.title.isNotBlank()
    }

    fun detach(file: AppFile): GalleryEditorUiState {
        return copy(
            pickedImages = pickedImages.filterNot { f ->
                f == file
            }
        )
    }

}

@HiltViewModel
class GalleryEditorViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    val filePropertyDataSource: FilePropertyDataSource,
    val accountRepository: AccountRepository,
    private val taskExecutor: CreateGalleryTaskExecutor,
    val driveFileRepository: DriveFileRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {


    companion object;

    private val _state = MutableStateFlow(
        GalleryEditorUiState(
            type = EditType.Create(accountId = null),
        )
    )

    val state: StateFlow<GalleryEditorUiState> = _state

    val logger = loggerFactory.create("GalleryEditorVM")

    val pickedImages = state.map {
        it.pickedImages.map { appFile ->
            when (appFile) {
                is AppFile.Local -> FilePreviewSource.Local(appFile)
                is AppFile.Remote -> FilePreviewSource.Remote(
                    appFile,
                    fileProperty = driveFileRepository.find(appFile.id)
                )
            }
        }
    }.catch {
        logger.error("ファイル取得失敗", it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    init {

        viewModelScope.launch {
            state.map {
                it.type
            }.distinctUntilChanged().map {
                when (it) {
                    is EditType.Update -> {
                        val gallery = galleryRepository.find(it.postId)
                        val files = filePropertyDataSource.findIn(gallery.fileIds).getOrNull()
                        state.value.copy(
                            title = gallery.title,
                            description = gallery.description,
                            isSensitive = gallery.isSensitive,
                            pickedImages = files?.map { file -> file.id }?.map { fileId ->
                                AppFile.Remote(fileId)
                            } ?: emptyList(),
                        )
                    }
                    is EditType.Create -> {
                        GalleryEditorUiState(type = it)
                    }
                }
            }.catch { error ->
                logger.error("致命的なエラーが発生", error)
            }.collect { newState ->
                _state.update {
                    newState
                }
            }
        }
    }

    fun setType(type: EditType) {
        _state.update {
            it.copy(
                type = type
            )
        }
    }

    fun detach(file: AppFile) {
        _state.update {
            it.detach(file)
        }
    }

    fun toggleSensitive(file: AppFile) {
        when (file) {
            is AppFile.Local -> {

                _state.update { state ->
                    state.copy(
                        pickedImages = state.pickedImages.map {
                            if (it === file) {
                                it.copy(isSensitive = !file.isSensitive)
                            } else {
                                it
                            }
                        }
                    )
                }
            }
            is AppFile.Remote -> {
                viewModelScope.launch {
                    runCancellableCatching {
                        driveFileRepository.toggleNsfw(file.id)
                    }.onFailure {
                        logger.info("sensitiveの切り替えに失敗しました。", e = it)
                    }
                }
            }
        }
    }


    fun addFilePropertyIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch {
            filePropertyDataSource.findIn(ids).onSuccess { files ->
                _state.update { state ->
                    val list = state.pickedImages.toMutableList().also { list ->
                        list.addAll(
                            files.map {
                                AppFile.Remote(it.id)
                            }
                        )
                    }
                    state.copy(
                        pickedImages = list
                    )
                }
            }

        }
    }

    fun addFile(file: AppFile) {
        _state.update { state ->
            state.copy(
                pickedImages = state.pickedImages.toMutableList().also { mutable ->
                    mutable.add(file)
                }
            )
        }
    }

    fun validate(): Boolean {
        return this.state.value.validate()
    }

    fun setTitle(text: String?) {
        _state.update {
            it.copy(title = text ?: "")
        }
    }

    fun setDescription(text: String?) {
        _state.update {
            it.copy(description = text)
        }
    }

    fun toggleSensitive() {
        _state.update {
            it.copy(isSensitive = !it.isSensitive)
        }
    }

    suspend fun save() {
        val files = state.value.pickedImages
        val title = state.value.title
        val description = state.value.description ?: ""
        val isSensitive = state.value.isSensitive
        if (validate()) {
            val create = CreateGalleryPost(
                title,
                getAccount(),
                files,
                description,
                isSensitive
            )
            taskExecutor.dispatch(create.toTask(galleryRepository))
        }

    }

    private var _accountId: Long? = null
    private val _accountLock = Mutex()


    private suspend fun getAccount(): Account {
        _accountLock.withLock {
            if (_accountId == null) {
                return accountRepository.getCurrentAccount().getOrThrow().also {
                    _accountId = it.accountId
                }
            }
            return accountRepository.get(_accountId!!).getOrThrow()
        }
    }
}