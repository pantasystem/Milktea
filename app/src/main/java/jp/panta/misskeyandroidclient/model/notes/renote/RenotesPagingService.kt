package jp.panta.misskeyandroidclient.model.notes.renote

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.notes.FindRenotes
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import retrofit2.Response
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.sync.withLock

interface RenotesPagingService {
    val state: Flow<PageableState<List<Renote>>>
    suspend fun next()
    suspend fun refresh()
    suspend fun clear()
}

class RenotesPagingServiceImpl(
    targetNoteId: Note.Id,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val encryption: Encryption,

) : RenotesPagingService{

    private val pagingImpl = RenotesPagingImpl(targetNoteId, misskeyAPIProvider, accountRepository, noteDataSourceAdder, encryption)
    private val controller = PreviousPagingController(pagingImpl, pagingImpl, pagingImpl, pagingImpl)

    override val state: Flow<PageableState<List<Renote>>>
        get() = pagingImpl.state

    override suspend fun clear() {
        pagingImpl.mutex.withLock {
            pagingImpl.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override suspend fun next() {
        controller.loadPrevious()
    }

    override suspend fun refresh() {
        this.clear()
        this.next()
    }
}
class RenotesPagingImpl(
    private val targetNoteId: Note.Id,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository,
    val noteDataSourceAdder: NoteDataSourceAdder,
    val encryption: Encryption,
) : PreviousLoader<NoteDTO>,
    EntityConverter<NoteDTO, Renote>,
    StateLocker,
    PaginationState<Renote>,
    IdGetter<String>
{

    private val _state: MutableStateFlow<PageableState<List<Renote>>> = MutableStateFlow(PageableState.Fixed(StateContent.NotExist()))
    override val state: Flow<PageableState<List<Renote>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun loadPrevious(): Response<List<NoteDTO>> {
        val account = accountRepository.get(targetNoteId.accountId)
        val i = account.getI(encryption)

        return misskeyAPIProvider.get(account.instanceDomain).renotes(FindRenotes(i = i, noteId = targetNoteId.noteId, untilId = getUntilId()))
    }

    override suspend fun addAll(list: List<NoteDTO>): List<Renote> {
        val account = accountRepository.get(targetNoteId.accountId)
        return list.map {
            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
        }.map {
            if(it.isQuote()) {
                Renote.Quote(it.id)
            }else{
                Renote.Normal(it.id)
            }
        }
    }

    override suspend fun getSinceId(): String? {
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()?.noteId?.noteId
    }

    override suspend fun getUntilId(): String? {
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()?.noteId?.noteId
    }

    override fun getState(): PageableState<List<Renote>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Renote>>) {
        _state.value = state
    }


}

fun MiCore.createRenotesPagingService(targetNoteId: Note.Id): RenotesPagingService {
    return RenotesPagingServiceImpl(
        targetNoteId,
        this.getMisskeyAPIProvider(),
        this.getAccountRepository(),
        NoteDataSourceAdder(
            this.getUserDataSource(),
            this.getNoteDataSource(),
            this.getFilePropertyDataSource()
        ),
        this.getEncryption(),
    )
}