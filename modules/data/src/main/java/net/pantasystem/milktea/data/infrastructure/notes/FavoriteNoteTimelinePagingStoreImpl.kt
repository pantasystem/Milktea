package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note


class FavoriteNoteTimelinePagingStoreImpl(
    val pageableTimeline: Pageable.Favorite,
    val noteAdder: NoteDataSourceAdder,
    val getAccount: suspend () -> Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
) : EntityConverter<Favorite, Note.Id>, PreviousLoader<Favorite>, FutureLoader<Favorite>,
    IdGetter<String>, StateLocker, TimelinePagingBase {

    var favoriteIdAndNoteIdMap = mutableMapOf<Note.Id, String>()

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun convertAll(list: List<Favorite>): List<Note.Id> {
        val account = getAccount.invoke()

        val fabIdAndNoteId = list.map {
            noteAdder.addNoteDtoToDataSource(account, it.note).id to it.id
        }
        favoriteIdAndNoteIdMap.putAll(fabIdAndNoteId.toMap())
        return fabIdAndNoteId.map {
            it.first
        }
    }

    override suspend fun getSinceId(): String? {
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()?.let {
            favoriteIdAndNoteIdMap[it]
        }
    }

    override suspend fun getUntilId(): String? {
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()?.let {
            favoriteIdAndNoteIdMap[it]
        }
    }

    override suspend fun loadFuture(): Result<List<Favorite>> {
        val ac = getAccount.invoke()
        return runCancellableCatching {
            misskeyAPIProvider.get(getAccount.invoke()).favorites(
                NoteRequest.Builder(pageableTimeline, ac.token, limit = LIMIT)
                    .build(NoteRequest.Conditions(sinceId = getSinceId()))
            ).throwIfHasError().body()!!
        }
    }

    override suspend fun loadPrevious(): Result<List<Favorite>> {
        return runCancellableCatching {
            val ac = getAccount.invoke()
            misskeyAPIProvider.get(getAccount.invoke()).favorites(
                NoteRequest.Builder(pageableTimeline, ac.token, limit = LIMIT)
                    .build(NoteRequest.Conditions(untilId = getUntilId()))
            ).throwIfHasError().body()!!
        }
    }


    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }
}

