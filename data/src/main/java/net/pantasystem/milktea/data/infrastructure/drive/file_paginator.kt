package net.pantasystem.milktea.data.infrastructure.drive

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.drive.RequestFile
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.api.misskey.throwIfHasError
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import retrofit2.Response


class FilePropertyPagingStore @AssistedInject constructor(
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    encryption: Encryption,
    @Assisted private var currentDirectoryId: String?,
    @Assisted private val getAccount: suspend () -> Account,
) {

    @AssistedFactory
    interface AssistedStoreFactory {
        fun create(currentDirectoryId: String?, getAccount: suspend () -> Account): FilePropertyPagingStore
    }
    companion object;
    private val filePropertyPagingImpl = FilePropertyPagingImpl(
        misskeyAPIProvider,
        encryption,
        filePropertyDataSource,
        {
            getAccount.invoke()
        },
        {
            currentDirectoryId
        },
    )

    private val previousPagingController: PreviousPagingController<FilePropertyDTO, FileProperty.Id> =
        PreviousPagingController(
            filePropertyPagingImpl,
            filePropertyPagingImpl,
            filePropertyPagingImpl,
            filePropertyPagingImpl
        )

    val state = this.filePropertyPagingImpl.state

    val isLoading: Boolean get() = this.filePropertyPagingImpl.mutex.isLocked

    suspend fun loadPrevious() {
        previousPagingController.loadPrevious()
    }

    suspend fun clear() {
        this.filePropertyPagingImpl.mutex.withLock {
            this.filePropertyPagingImpl.setState(PageableState.Loading.Init())
        }
    }

    suspend fun setCurrentDirectory(directory: Directory?) {
        this.clear()
        this.currentDirectoryId = directory?.id
    }

    /**
     * DriveFileが作成されたタイミングで呼び出される
     */
    fun onCreated(id: FileProperty.Id) {
        filePropertyPagingImpl.setState(
            filePropertyPagingImpl.getState().convert {
                it.toMutableList().also { list ->
                    list.add(0, id)
                }
            }
        )
    }
}


class FilePropertyPagingImpl(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val encryption: Encryption,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val getAccount: suspend () -> Account,
    private val getCurrentFolderId: () -> String?,
) : PaginationState<FileProperty.Id>,
    IdGetter<String>, PreviousLoader<FilePropertyDTO>,
    EntityConverter<FilePropertyDTO, FileProperty.Id>,
    StateLocker {

    private val _state = MutableStateFlow<PageableState<List<FileProperty.Id>>>(
        PageableState.Fixed(
            StateContent.NotExist()
        )
    )
    override val state: Flow<PageableState<List<FileProperty.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override fun setState(state: PageableState<List<FileProperty.Id>>) {
        _state.value = state
    }

    override fun getState(): PageableState<List<FileProperty.Id>> {
        return _state.value
    }

    override suspend fun getSinceId(): String? {
        return (getState().content as? StateContent.Exist<List<FileProperty.Id>>)?.rawContent?.firstOrNull()?.fileId
    }

    override suspend fun getUntilId(): String? {
        return (getState().content as? StateContent.Exist<List<FileProperty.Id>>)?.rawContent?.lastOrNull()?.fileId
    }

    override suspend fun loadPrevious(): Response<List<FilePropertyDTO>> {
        return misskeyAPIProvider.get(getAccount.invoke().instanceDomain).getFiles(
            RequestFile(
                folderId = getCurrentFolderId.invoke(),
                untilId = this.getUntilId(),
                i = getAccount.invoke().getI(encryption),
                limit = 20
            )
        ).throwIfHasError()
    }

    override suspend fun convertAll(list: List<FilePropertyDTO>): List<FileProperty.Id> {
        val entities = list.map {
            it.toFileProperty(getAccount.invoke())
        }
        filePropertyDataSource.addAll(entities)
        return entities.map {
            it.id
        }
    }

}

