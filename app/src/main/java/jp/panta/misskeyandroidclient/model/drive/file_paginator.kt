package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.api.drive.RequestFile
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response

class FilePropertyPagingState : PaginationState<FileProperty.Id>, IdGetter<String> {

    private val _state = MutableStateFlow<PageableState<List<FileProperty.Id>>>(PageableState.Fixed(StateContent.NotExist()))
    override val state: Flow<PageableState<List<FileProperty.Id>>>
        get() = _state

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

}

class FilePropertyAdder(
    private val accountLoader: suspend ()-> Account,
    private val filePropertyDataSource: FilePropertyDataSource
) : EntityAdder<FilePropertyDTO, FileProperty.Id> {
    override suspend fun addAll(list: List<FilePropertyDTO>): List<FileProperty.Id> {
        val entities = list.map {
            it.toFileProperty(accountLoader.invoke())
        }
        filePropertyDataSource.addAll(entities)
        return entities.map {
            it.id
        }
    }
}

class FilePropertyLoader (
    private val idGetter: IdGetter<String>,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val getAccount: suspend ()-> Account,
    private val getCurrentFolderId: ()-> String,
    private val encryption: Encryption
) : PreviousLoader<FilePropertyDTO>{

    override suspend fun loadPrevious(): Response<List<FilePropertyDTO>> {
        return misskeyAPIProvider.get(getAccount.invoke().instanceDomain).getFiles(
            RequestFile(
                folderId = getCurrentFolderId.invoke(),
                untilId = idGetter.getUntilId(),
                i = getAccount.invoke().getI(encryption)
            )
        ).throwIfHasError()
    }
}