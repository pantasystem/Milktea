package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteTranslationStore


class PlaneNoteViewDataCache(
    private val getAccount: suspend () -> Account,
    private val noteCaptureAdapter: NoteCaptureAPIAdapter,
    private val translationStore: NoteTranslationStore
) {

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

    suspend fun put(viewData: PlaneNoteViewData): PlaneNoteViewData {
        return lock.withLock {
            cache[viewData.note.note.id] = viewData
            viewData
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
        return if (relation.reply == null) {
            PlaneNoteViewData(
                relation,
                getAccount(),
                noteCaptureAdapter,
                translationStore
            )
        } else {
            HasReplyToNoteViewData(
                relation,
                getAccount(),
                noteCaptureAdapter,
                translationStore
            )
        }
    }

    suspend fun onDeleted(noteId: Note.Id) {
        lock.withLock {
            cache.values.filter {
                it.toShowNote.note.id == noteId
            }.map { note ->
                note.job?.cancel()
                cache.remove(note.id)
            }
        }
    }
}
