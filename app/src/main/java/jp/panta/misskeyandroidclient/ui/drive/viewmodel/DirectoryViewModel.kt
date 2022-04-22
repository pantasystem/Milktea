package jp.panta.misskeyandroidclient.ui.drive.viewmodel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.Encryption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.api.misskey.drive.RequestFolder
import net.pantasystem.milktea.api.misskey.throwIfHasError
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.drive.CreateDirectory
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository
import net.pantasystem.milktea.model.drive.DriveStore


class DirectoryViewModel @AssistedInject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val encryption: Encryption,
    loggerFactory: Logger.Factory,
    private val accountRepository: AccountRepository,
    private val driveDirectoryRepository: DriveDirectoryRepository,
    @Assisted private val driveStore: DriveStore,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(driveStore: DriveStore): DirectoryViewModel
    }

    companion object;

    private val accountWatcher by lazy {
        CurrentAccountWatcher(driveStore.state.value.accountId, accountRepository)
    }
    val foldersLiveData = MutableLiveData<List<DirectoryViewData>>()

    val isRefreshing = MutableLiveData(false)


    private var isLoading = false

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    private val logger = loggerFactory.create("DirectoryVM")

    init {
        driveStore.state.map {
            it.accountId to it.path.path
        }.distinctUntilChanged().onEach {
            loadInit()
        }.catch { e ->
            logger.warning("アカウント変更伝達処理中にエラー", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun loadInit() {
        if (isLoading) {
            return
        }
        isLoading = true

        isRefreshing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = accountWatcher.getAccount()
                val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
                val rawList = misskeyAPI.getFolders(
                    RequestFolder(
                        i = account.getI(encryption),
                        folderId = driveStore.state.value.path.path.lastOrNull()?.id,
                        limit = 20
                    )
                ).throwIfHasError().body()
                requireNotNull(rawList)
                require(rawList.isNotEmpty())
                rawList.map {
                    DirectoryViewData(it)
                }
            }.onSuccess {
                foldersLiveData.postValue(it)
            }.onFailure {
                foldersLiveData.postValue(emptyList())
                logger.debug("初期ロードに失敗しました")
            }
            isLoading = false
            isRefreshing.postValue(false)
        }
    }

    fun loadNext() {
        if (isLoading) {
            return
        }
        isLoading = true
        val beforeList = foldersLiveData.value
        val untilId = beforeList?.lastOrNull()?.id
        if (beforeList == null || untilId == null) {
            isLoading = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = accountWatcher.getAccount()
                val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
                val request = RequestFolder(
                    i = account.getI(encryption),
                    folderId = driveStore.state.value.path.path.lastOrNull()?.id,
                    limit = 20,
                    untilId = untilId
                )

                misskeyAPI.getFolders(request).throwIfHasError().body()?.map {
                    DirectoryViewData(it)
                }

            }.onSuccess { viewDataList ->
                requireNotNull(viewDataList)
                val newList = ArrayList<DirectoryViewData>(beforeList).apply {
                    addAll(viewDataList)
                }
                foldersLiveData.postValue(newList)
            }.onFailure {
                logger.debug("loadNext中にエラー発生", e = it)
            }
            isLoading = false
        }

    }

    fun createDirectory(folderName: String) {
        if (folderName.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                driveDirectoryRepository.create(
                    CreateDirectory(
                        accountId = accountWatcher.getAccount().accountId,
                        directoryName = folderName
                    )
                ).onFailure {
                    Log.e("FolderViewModel", "error create folder", it)
                    _error.value = it
                }
            }

        }

    }
}

@Suppress("UNCHECKED_CAST")
fun DirectoryViewModel.Companion.provideViewModel(
    assistedFactory: DirectoryViewModel.ViewModelAssistedFactory,
    driveStore: DriveStore,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(driveStore) as T
    }

}