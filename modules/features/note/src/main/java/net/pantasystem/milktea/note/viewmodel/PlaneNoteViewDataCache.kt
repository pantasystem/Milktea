package net.pantasystem.milktea.note.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common_android_ui.TextType
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.setting.LocalConfigRepository
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
    private val noteDataSource: NoteDataSource,
    private val configRepository: LocalConfigRepository,
    private val emojiRepository: CustomEmojiRepository,
) {

    @Singleton
    class Factory @Inject constructor(
        private val noteCaptureAdapter: NoteCaptureAPIAdapter,
        private val translationStore: NoteTranslationStore,
        private val urlPreviewStoreProvider: UrlPreviewStoreProvider,
        private val noteRelationGetter: NoteRelationGetter,
        private val noteDataSource: NoteDataSource,
        private val configRepository: LocalConfigRepository,
        private val emojiRepository: CustomEmojiRepository,
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
                noteDataSource,
                configRepository,
                emojiRepository,
            )
        }
    }

    private val lock = Mutex()
    private var cache = mutableMapOf<Note.Id, PlaneNoteViewData>()

    suspend fun get(relation: NoteRelation): PlaneNoteViewData {
        return lock.withLock {
            getUnThreadSafe(relation)
        }
    }

    private suspend fun useIn(relations: List<NoteRelation>): List<PlaneNoteViewData> {
        return lock.withLock {
            val viewDataList = relations.map {
                getUnThreadSafe(it)
            }
            viewDataList
        }
    }

    suspend fun useByIds(
        ids: List<Note.Id>,
        isGoneAfterRenotes: Boolean = true,
        isReleaseUnUsedResource: Boolean = true,
    ): List<PlaneNoteViewData> {
        val exists: List<PlaneNoteViewData>
        val notExistsIds: List<Note.Id>
        val removed: List<PlaneNoteViewData>
        lock.withLock {
            exists = ids.mapNotNull {
                cache[it]
            }

            notExistsIds = ids.filter {
                cache[it] == null
            }

            val idHash = ids.toSet()

            removed = if (isReleaseUnUsedResource) {
                cache.filterNot {
                    idHash.contains(it.key)
                }.map {
                    it.value
                }
            } else {
                emptyList()
            }
        }

        val relations = noteRelationGetter.getIn(notExistsIds)
        val newList = useIn(relations)
        val map = (exists + newList).associateBy {
            it.id
        }
        val notes = ids.mapNotNull {
            map[it]
        }

        if (isReleaseUnUsedResource) {
            lock.withLock {
                cache = map.toMutableMap()
            }
            removed.forEach {
                it.job?.cancel()
            }
        }


        // NOTE: リノートが連続している場合は、そのコンテンツを省略するフラグを立てる処理
        if (isGoneAfterRenotes) {
            for (i in 0 until notes.size - 1) {
                val current = notes[i]
                val next = notes[i + 1]
                current.isOnlyVisibleRenoteStatusMessage.value = (current.note.note.isRenoteOnly()
                        && (
                        current.note.note.renoteId == next.note.note.renoteId
                                || current.note.note.renoteId == next.note.note.id))
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
        emojiRepository.findBy(account.getHost())
        return if (relation.reply == null) {
            PlaneNoteViewData(
                relation,
                account,
                translationStore,
                noteDataSource,
                configRepository,
                emojiRepository,
                coroutineScope,
            )
        } else {
            HasReplyToNoteViewData(
                relation,
                account,
                translationStore,
                noteDataSource,
                configRepository,
                emojiRepository,
                coroutineScope
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
        if (configRepository.get().getOrNull()?.isEnableStreamingAPIAndNoteCapture == false) {
            return
        }
        val scope = coroutineScope + Dispatchers.IO
        this.capture(noteCaptureAdapter) { flow ->
            flow.onEach {
                if (it is NoteDataSource.Event.Deleted) {
                    onDeleted(it.noteId)
                }
            }.launchIn(scope)
        }
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

    suspend fun suspendNoteCapture() {
        lock.withLock {
            cache.values.map {
                it.job?.cancel()
            }
        }
    }

    suspend fun captureNotes() {
        lock.withLock {
            cache.values.filterNot {
                it.job?.isActive == true
            }.map {
                it.captureNotes()
            }
        }
    }

    suspend fun captureNotesBy(ids: List<Note.Id>) {
        lock.withLock {
            cache.values.filterNot {
                it.job?.isActive == true
            }.filter {
                ids.contains(it.id)
            }.map {
                it.captureNotes()
            }
        }
    }

}
