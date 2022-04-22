package net.pantasystem.milktea.data.infrastructure.drive

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.api.misskey.drive.RequestFile
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.FilePropertyPagingStore
import retrofit2.Response
import javax.inject.Inject


class FilePropertyPagingStoreImpl @Inject constructor(
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    encryption: Encryption,
) : FilePropertyPagingStore {

    private var currentDirectoryId: String? = null

    private var currentAccount: Account? = null

    companion object;
    private val filePropertyPagingImpl = FilePropertyPagingImpl(
        misskeyAPIProvider,
        encryption,
        filePropertyDataSource,
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

