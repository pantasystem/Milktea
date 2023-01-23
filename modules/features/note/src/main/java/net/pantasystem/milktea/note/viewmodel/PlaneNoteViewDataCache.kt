package net.pantasystem.milktea.note.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common_android.TextType
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.url.UrlPreviewLoadTask
import net.pantasystem.milktea.model.url.UrlPreviewStore
import net.pantasystem.milktea.model.url.UrlPreviewStoreProvider
import javax.inject.Inject
import javax.inject.Singleton


class PlaneNoteViewDataCache(
    private val getAccount: suspend () -> Account,
    private val noteCaptureAdapter: NoteCaptureAPIAdapter,
    private val translationStore: NoteTranslationStore,
    private val GetUrlPreviewStore: suspend (Account) -> UrlPreviewStore?,
    private val coroutineScope: CoroutineScope,
    private val noteRelationGetter: NoteRelationGetter,
    private val metaRepository: MetaRepository,
) {

    @Singleton
    class Factory @Inject constructor(
        private val noteCaptureAdapter: NoteCaptureAPIAdapter,
        private val translationStore: NoteTranslationStore,
        private val urlPreviewStoreProvider: UrlPreviewStoreProvider,
        private val noteRelationGetter: NoteRelationGetter,
        private val metaRepository: MetaRepository,
    ) {
        fun create(
            getAccount: suspend () -> Account,
            coroutineScope: CoroutineScope,
        ): PlaneNoteViewDataCache {
            return PlaneNoteViewDataCache(
                getAccount,
                noteCaptureAdapter,
                translationStore,
                {
                    urlPreviewStoreProvider.getUrlPreviewStore(it)
                },
                coroutineScope,
                noteRelationGetter,
                metaRepository
            )
        }
    }

    private val lock = Mutex()
    private val cache = mutableMapOf<Note.Id, PlaneNoteViewData>()

    suspend fun get(relation: NoteRelation): PlaneNoteViewData {
        return lock.withLock {
            getUnThreadSafe(relation)
        }
    }

    suspend fun getIn(relations: List<NoteRelation>): List<PlaneNoteViewData> {
        return lock.withLock {
            relations.map {
                getUnThreadSafe(it)
            }
        }
    }

    suspend fun getByIds(
        ids: List<Note.Id>,
        isGoneAfterRenotes: Boolean = true
    ): List<PlaneNoteViewData> {
        val notExistsIds = lock.withLock {
            ids.filter {
                cache[it] == null
            }
        }
        val exists = lock.withLock {
            ids.mapNotNull {
                cache[it]
            }
        }
        val relations = noteRelationGetter.getIn(notExistsIds)
        val newList = getIn(relations)
        val map = (exists + newList).associateBy {
            it.id
        }
        val notes = ids.mapNotNull {
            map[it]
        }

        // NOTE: リノートが連続している場合は、そのコンテンツを省略するフラグを立てる処理
        if (isGoneAfterRenotes) {
            for (i in 0 until notes.size - 1) {
                val current = notes[i]
                val next = notes[i + 1]
                if (current.note.note.isRenoteOnly()
                    && next.note.note.isRenoteOnly()
                    && current.note.note.renoteId == next.note.note.renoteId
                ) {
                    current.isOnlyVisibleRenoteStatusMessage.postValue(true)
                } else {
                    current.isOnlyVisibleRenoteStatusMessage.postValue(false)
                }
            }
        }
        return notes
    }

    suspend fun put(viewData: PlaneNoteViewData): PlaneNoteViewData {
        return lock.withLock {
            cache[viewData.note.note.id] = viewData
            viewData
        }
    }

    suspend fun clear() {
        return lock.withLock {
            releaseAll(cache.values.toList())
            cache.clear()
        }
    }

    private fun releaseAll(list: List<PlaneNoteViewData>) {
        list.map { note ->
            note.job?.cancel()
        }
    }

    private suspend fun getUnThreadSafe(relation: NoteRelation): PlaneNoteViewData {
        val note = cache[relation.note.id]

        if (note == null) {
            cache[relation.note.id] = createViewData(relation)
        }
        return cache[relation.note.id]!!
    }

    private suspend fun createViewData(relation: NoteRelation): PlaneNoteViewData {
        val account = getAccount()
        return if (relation.reply == null) {
            PlaneNoteViewData(
                relation,
                account,
                noteCaptureAdapter,
                translationStore,
                metaRepository.get(account.normalizedInstanceDomain)?.emojis ?: emptyList()
            )
        } else {
            HasReplyToNoteViewData(
                relation,
                account,
                noteCaptureAdapter,
                translationStore,
                metaRepository.get(account.normalizedInstanceDomain)?.emojis ?: emptyList()
            )
        }.also {
            it.captureNotes()
            loadUrlPreview(it)
        }
    }

    private suspend fun onDeleted(noteId: Note.Id) {
        lock.withLock {
            cache.values.filter {
                it.toShowNote.note.id == noteId
            }.map { note ->
                note.job?.cancel()
                cache.remove(note.id)
            }
        }
    }

    private fun PlaneNoteViewData.captureNotes() {
        val scope = coroutineScope + Dispatchers.IO
        this.job = this.eventFlow.onEach {
            if (it is NoteDataSource.Event.Deleted) {
                onDeleted(it.noteId)
            }
        }.launchIn(scope)
    }

    private suspend fun loadUrlPreview(note: PlaneNoteViewData) {
        (note.textNode as? TextType.Misskey?)?.root?.getUrls()?.let { urls ->
            UrlPreviewLoadTask(
                GetUrlPreviewStore.invoke(getAccount.invoke()),
                urls,
                coroutineScope + Dispatchers.IO,
            ).load(note.urlPreviewLoadTaskCallback)
        }
    }
}
