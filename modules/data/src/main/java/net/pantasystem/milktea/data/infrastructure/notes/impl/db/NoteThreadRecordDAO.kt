package net.pantasystem.milktea.data.infrastructure.notes.impl.db

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.notes.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NoteThreadRecordDAO @Inject constructor(
    private val boxStore: BoxStore,
) {

    private val noteThreadContextBox: Box<ThreadRecord> by lazy {
        boxStore.boxFor()
    }

    open suspend fun add(context: ThreadRecord) {
        boxStore.awaitCallInTx {
            val exists = noteThreadContextBox.query().equal(
                ThreadRecord_.targetNoteIdAndAccountId,
                context.targetNoteIdAndAccountId,
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().findFirst()
            when (exists) {
                null -> noteThreadContextBox.put(context)
                else -> noteThreadContextBox.put(context.copy(id = exists.id))
            }
        }

    }

    open suspend fun update(context: ThreadRecord) {
        boxStore.awaitCallInTx {
            noteThreadContextBox.put(context)
        }
    }

    open suspend fun appendBlank(noteId: Note.Id): ThreadRecord {
        return boxStore.awaitCallInTx {
            val exists = noteThreadContextBox.query().equal(
                ThreadRecord_.targetNoteIdAndAccountId,
                NoteRecord.generateAccountAndNoteId(noteId),
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().findFirst()
            when (exists) {
                null -> {
                    val new = ThreadRecord(
                        targetNoteId = noteId.noteId,
                        accountId = noteId.accountId,
                        targetNoteIdAndAccountId = NoteRecord.generateAccountAndNoteId(noteId)
                    )
                    noteThreadContextBox.put(new)
                    new
                }
                else -> exists
            }
        }!!
    }


    open suspend fun clearRelation(targetNote: Note.Id) {
        boxStore.awaitCallInTx {
            findBy(targetNote)?.also {
                it.ancestors.clear()
                it.descendants.clear()
                noteThreadContextBox.put(it)
            }
        }
    }

    open fun findBy(noteId: Note.Id): ThreadRecord? {
        return noteThreadContextBox.query().equal(
            ThreadRecord_.targetNoteIdAndAccountId,
            NoteRecord.generateAccountAndNoteId(noteId),
            QueryBuilder.StringOrder.CASE_SENSITIVE
        ).build().findFirst()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun observeBy(noteId: Note.Id): Flow<List<ThreadRecord>> {
        return noteThreadContextBox.query().equal(
            ThreadRecord_.targetNoteIdAndAccountId,
            NoteRecord.generateAccountAndNoteId(noteId),
            QueryBuilder.StringOrder.CASE_SENSITIVE
        ).build().subscribe().toFlow()
    }
}