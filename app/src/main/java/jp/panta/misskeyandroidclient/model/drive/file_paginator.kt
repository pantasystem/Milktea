package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.api.drive.RequestFile
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response


class FilePropertyPagingStore(
    private var currentDirectoryId: String?,
    private val getAccount: suspend () -> Account,
    private val accountRepository: AccountRepository,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    encryption: Encryption,

) {

    private val filePropertyPagingImpl = FilePropertyPagingImpl(
        misskeyAPIProvider,
        {
            getAccount.invoke()
        },
        {
            currentDirectoryId
        },
        encryption,
        filePropertyDataSource
    )

    private val previousPagingController: PreviousPagingController<FilePropertyDTO, FileProperty.Id> = PreviousPagingController(
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

}

fun MiCore.filePropertyPagingStore(getAccount: suspend () -> Account, currentDirectoryId: String?) : FilePropertyPagingStore{
    return FilePropertyPagingStore(
        currentDirectoryId,
        getAccount,
        this.getAccountRepository(),
        this.getMisskeyAPIProvider(),
        this.getFilePropertyDataSource(),
        this.getEncryption()
    )
}
class FilePropertyPagingImpl(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val getAccount: suspend ()-> Account,
    private val getCurrentFolderId: ()-> String?,
    private val encryption: Encryption,
    private val filePropertyDataSource: FilePropertyDataSource
) : PaginationState<FileProperty.Id>,
    IdGetter<String>, PreviousLoader<FilePropertyDTO>,
    EntityAdder<FilePropertyDTO, FileProperty.Id>,
    StateLocker
{

    private val _state = MutableStateFlow<PageableState<List<FileProperty.Id>>>(PageableState.Fixed(StateContent.NotExist()))
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

    override suspend fun addAll(list: List<FilePropertyDTO>): List<FileProperty.Id> {
        val entities = list.map {
            it.toFileProperty(getAccount.invoke())
        }
        filePropertyDataSource.addAll(entities)
        return entities.map {
            it.id
        }
    }

}

