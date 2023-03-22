package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.drive.RequestFile
import net.pantasystem.milktea.app_store.drive.FilePropertyPagingStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.FilePropertyDTOEntityConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import javax.inject.Inject


class FilePropertyPagingStoreImpl @Inject constructor(
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
) : FilePropertyPagingStore {

    private var currentDirectoryId: String? = null

    private var currentAccount: Account? = null

    companion object;
    private val filePropertyPagingImpl = FilePropertyPagingImpl(
        misskeyAPIProvider,
        filePropertyDataSource,
        filePropertyDTOEntityConverter,
        {
            currentAccount?: throw UnauthorizedException()
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

    override val state = this.filePropertyPagingImpl.state

    override val isLoading: Boolean get() = this.filePropertyPagingImpl.mutex.isLocked

    override suspend fun loadPrevious() {
        previousPagingController.loadPrevious()
    }

    override suspend fun clear() {
        this.filePropertyPagingImpl.mutex.withLock {
            this.filePropertyPagingImpl.setState(PageableState.Loading.Init())
        }
    }

    override suspend fun setCurrentDirectory(directory: Directory?) {
        this.clear()
        this.currentDirectoryId = directory?.id
    }

    override suspend fun setCurrentAccount(account: Account?) {
        this.clear()
        this.currentAccount = account
    }

    /**
     * DriveFileが作成されたタイミングで呼び出される
     */
    override fun onCreated(id: FileProperty.Id) {
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
    private val filePropertyDataSource: FilePropertyDataSource,
    private val filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
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

    override suspend fun loadPrevious(): Result<List<FilePropertyDTO>> {
        return runCancellableCatching {
            misskeyAPIProvider.get(getAccount.invoke().normalizedInstanceUri).getFiles(
                RequestFile(
                    folderId = getCurrentFolderId.invoke(),
                    untilId = this.getUntilId(),
                    i = getAccount.invoke().token,
                    limit = 20
                )
            ).throwIfHasError().body()!!
        }
    }

    override suspend fun convertAll(list: List<FilePropertyDTO>): List<FileProperty.Id> {
        val entities = list.map {
            filePropertyDTOEntityConverter.convert(it, getAccount())
        }
        filePropertyDataSource.addAll(entities)
        return entities.map {
            it.id
        }
    }

}

