package net.pantasystem.milktea.data.infrastructure.note

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.FutureLoader
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.timeline.favorite.FavoriteTimelineRepository


internal class FavoriteNoteTimelinePagingStoreImpl(
    val getAccount: suspend () -> Account,
    private val favoriteTimelineRepository: FavoriteTimelineRepository,
) : EntityConverter<Note.Id, Note.Id>, PreviousLoader<Note.Id>, FutureLoader<Note.Id>,
    IdGetter<String>, StateLocker, TimelinePagingBase {

    private var untilId: String? = null
    private var sinceId: String? = null

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun convertAll(list: List<Note.Id>): List<Note.Id> {
        return list
    }

    override suspend fun getSinceId(): String? {
        return sinceId
    }

    override suspend fun getUntilId(): String? {
        return untilId
    }

    override suspend fun loadFuture(): Result<List<Note.Id>> {
        val ac = getAccount.invoke()
        return runCancellableCatching {

            val res = favoriteTimelineRepository.findLaterTimeline(
                ac.accountId,
                sinceId = getSinceId(),
                limit = LIMIT
            ).getOrThrow()
            if (!isEmpty() && res.timelineItems.isEmpty()) {
                return@runCancellableCatching emptyList()
            }
            sinceId = res.sinceId
            res.timelineItems
        }
    }

    override suspend fun loadPrevious(): Result<List<Note.Id>> {
        return runCancellableCatching {
            val ac = getAccount.invoke()
            val res = favoriteTimelineRepository.findPreviousTimeline(
                ac.accountId,
                untilId = getUntilId(),
                limit = LIMIT
            ).getOrThrow()
            if (!isEmpty() && res.timelineItems.isEmpty()) {
                return@runCancellableCatching emptyList()
            }
            untilId = res.untilId
            res.timelineItems
        }
    }


    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }

    private fun isEmpty(): Boolean {
        return when(val content = _state.value.content) {
            is StateContent.Exist -> content.rawContent.isEmpty()
            is StateContent.NotExist -> true
        }
    }
}


sealed interface FavoriteType {
    data class Misskey(val favorite: Favorite) : FavoriteType
    data class Mastodon(val status: TootStatusDTO) : FavoriteType
}

