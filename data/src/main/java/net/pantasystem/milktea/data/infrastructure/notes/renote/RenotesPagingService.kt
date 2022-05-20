package net.pantasystem.milktea.data.infrastructure.notes.renote

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.notes.FindRenotes
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.renote.Renote

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

    ) : RenotesPagingService {

    private val pagingImpl = RenotesPagingImpl(
        targetNoteId,
        misskeyAPIProvider,
        accountRepository,
        noteDataSourceAdder,
        encryption
    )
    private val controller =
        PreviousPagingController(pagingImpl, pagingImpl, pagingImpl, pagingImpl)

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
    IdGetter<String> {

    private val _state: MutableStateFlow<PageableState<List<Renote>>> =
        MutableStateFlow(PageableState.Fixed(StateContent.NotExist()))
    override val state: Flow<PageableState<List<Renote>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun loadPrevious(): Result<List<NoteDTO>> {
        return runCatching {
            val account = accountRepository.get(targetNoteId.accountId)
            val i = account.getI(encryption)

            misskeyAPIProvider.get(account.instanceDomain)
                .renotes(FindRenotes(i = i, noteId = targetNoteId.noteId, untilId = getUntilId()))
                .throwIfHasError().body()!!
        }
    }

    override suspend fun convertAll(list: List<NoteDTO>): List<Renote> {
        val account = accountRepository.get(targetNoteId.accountId)
        return list.map {
            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
        }.map {
            if (it.isQuote()) {
                Renote.Quote(it.id)
            } else {
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

