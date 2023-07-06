package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.misskey.drive.RequestFolder
import net.pantasystem.milktea.app_store.drive.DriveDirectoryPagingStore
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.drive.Directory
import javax.inject.Inject

class DriveDirectoryPagingStoreImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
    val encryption: Encryption,
) : DriveDirectoryPagingStore {
    private val pagingImpl by lazy {
        DriveDirectoryPagingImpl(
            misskeyAPIProvider,
            accountRepository,
            encryption,
        )
    }

    private val controller = PreviousPagingController(
        pagingImpl,
        pagingImpl,
        pagingImpl,
        pagingImpl,
    )

    override val state: Flow<PageableState<List<Directory>>>
        get() = pagingImpl.state

    override suspend fun clear() {
        pagingImpl.setState(PageableState.Fixed(StateContent.NotExist()))
    }

    override suspend fun loadPrevious(): Result<Int> {
        return controller.loadPrevious()
    }

    override suspend fun setAccount(account: Account?) {
        pagingImpl.setCurrentAccount(account)
        this.clear()
    }

    override suspend fun setCurrentDirectory(directory: Directory?) {
        pagingImpl.setCurrentDirectory(directory)
        this.clear()
    }

    override fun onCreated(directory: Directory) {
        if (pagingImpl.directory?.id == directory.parentId) {
            pagingImpl.setState(
                pagingImpl.getState().convert {
                    it.toMutableList().also { list ->
                        list.add(0, directory)
                    }
                }
            )
        }
    }

    override fun onDeleted(directory: Directory) {
        pagingImpl.setState(
            pagingImpl.getState().convert { list ->
                list.filterNot { it.id == directory.id }
            }
        )
    }
}


class DriveDirectoryPagingImpl(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
    val encryption: Encryption,
) : PaginationState<Directory>, IdGetter<String>,
    PreviousLoader<Directory>, EntityConverter<Directory, Directory>,
    StateLocker {

    private var account: Account? = null
    var directory: Directory? = null

    override val mutex: Mutex = Mutex()

    private val _state =
        MutableStateFlow<PageableState<List<Directory>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Directory>>>
        get() = _state

    override suspend fun convertAll(list: List<Directory>): List<Directory> {
        return list
    }

    override suspend fun getSinceId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.firstOrNull()?.id?.directoryId
    }

    override suspend fun getUntilId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.id?.directoryId
    }

    override suspend fun loadPrevious(): Result<List<Directory>> {
        return runCancellableCatching {
            val account = account ?: throw UnauthorizedException()
            misskeyAPIProvider.get(account)
                .getFolders(
                    RequestFolder(
                        i = account.token,
                        untilId = getUntilId(),
                        folderId = directory?.id?.directoryId
                    )
                )
                .throwIfHasError()
                .body()!!
                .map {
                    it.toModel(account)
                }
        }
    }

    override fun getState(): PageableState<List<Directory>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Directory>>) {
        _state.value = state
    }

    fun setCurrentAccount(account: Account?) {
        this.account = account
    }

    fun setCurrentDirectory(directory: Directory?) {
        this.directory = directory
    }


}