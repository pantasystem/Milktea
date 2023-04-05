package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note



internal class FavoriteNoteTimelinePagingStoreImpl(
    val pageableTimeline: Pageable.Favorite,
    val noteAdder: NoteDataSourceAdder,
    val getAccount: suspend () -> Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
) : EntityConverter<FavoriteType, Note.Id>, PreviousLoader<FavoriteType>, FutureLoader<FavoriteType>,
    IdGetter<String>, StateLocker, TimelinePagingBase {

    private var favoriteIdAndNoteIdMap = mutableMapOf<Note.Id, String>()

    private var mastodonUntilId: String? = null
    private var mastodonSinceId: String? = null

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun convertAll(list: List<FavoriteType>): List<Note.Id> {
        val account = getAccount.invoke()

        val fabIdAndNoteId = list.map {
            when(it) {
                is FavoriteType.Mastodon -> {
                    noteAdder.addTootStatusDtoIntoDataSource(account, it.status).id to it.status.id
                }
                is FavoriteType.Misskey -> {
                    noteAdder.addNoteDtoToDataSource(account, it.favorite.note).id to it.favorite.id
                }
            }
        }
        favoriteIdAndNoteIdMap.putAll(fabIdAndNoteId.toMap())
        return fabIdAndNoteId.map {
            it.first
        }
    }

    override suspend fun getSinceId(): String? {
        if (mastodonSinceId != null) {
            return mastodonSinceId
        }

        if (getAccount().instanceType == Account.InstanceType.MASTODON) {
            return null
        }

        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()?.let {
            favoriteIdAndNoteIdMap[it]
        }
    }

    override suspend fun getUntilId(): String? {
        if (mastodonUntilId != null) {
            return mastodonUntilId
        }
        if (getAccount().instanceType == Account.InstanceType.MASTODON) {
            return null
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()?.let {
            favoriteIdAndNoteIdMap[it]
        }
    }

    override suspend fun loadFuture(): Result<List<FavoriteType>> {
        val ac = getAccount.invoke()
        return runCancellableCatching {
            when(ac.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    misskeyAPIProvider.get(getAccount.invoke()).favorites(
                        NoteRequest.Builder(pageableTimeline, ac.token, limit = LIMIT)
                            .build(NoteRequest.Conditions(sinceId = getSinceId()))
                    ).throwIfHasError().body()!!.map {
                        FavoriteType.Misskey(it)
                    }
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    // NOTE: ページが末端であるかをチェックしている
                    if (getSinceId() == null && !isEmpty()) {
                        return@runCancellableCatching emptyList()
                    }

                    val res = mastodonAPIProvider.get(getAccount()).getFavouriteStatuses(
                        minId = getSinceId()
                    )
                    val linkHeader = res.headers()["link"]
                    mastodonSinceId = MastodonLinkHeaderDecoder(linkHeader).getMinId()
                    res.throwIfHasError().body()!!.map {
                        FavoriteType.Mastodon(it)
                    }
                }
            }
        }
    }

    override suspend fun loadPrevious(): Result<List<FavoriteType>> {
        return runCancellableCatching {
            val ac = getAccount.invoke()
            when(ac.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    misskeyAPIProvider.get(getAccount.invoke()).favorites(
                        NoteRequest.Builder(pageableTimeline, ac.token, limit = LIMIT)
                            .build(NoteRequest.Conditions(untilId = getUntilId()))
                    ).throwIfHasError().body()!!.map {
                        FavoriteType.Misskey(it)
                    }
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    // NOTE: ページが末端であるかをチェックしている
                    if (getUntilId() == null && !isEmpty()) {
                        return@runCancellableCatching emptyList()
                    }

                    val res = mastodonAPIProvider.get(getAccount()).getFavouriteStatuses(
                        maxId = getUntilId()
                    )
                    val linkHeader = res.headers()["link"]
                    mastodonUntilId = MastodonLinkHeaderDecoder(linkHeader).getMaxId()
                    res.throwIfHasError().body()!!.map {
                        FavoriteType.Mastodon(it)
                    }
                }
            }

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

