package net.pantasystem.milktea.data.infrastructure.note.impl.db

import io.objectbox.Box
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.data.infrastructure.BoxStoreHolder
import net.pantasystem.milktea.model.note.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NoteThreadRecordDAO @Inject constructor(
    private val boxStoreHolder: BoxStoreHolder,
) {

    private val noteThreadContextBox: Box<ThreadRecord> by lazy {
        boxStoreHolder.boxStore.boxFor()
    }

    open suspend fun add(context: ThreadRecord) {
        boxStoreHolder.boxStore.awaitCallInTx {
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
        boxStoreHolder.boxStore.awaitCallInTx {
            noteThreadContextBox.put(context)
        }
    }

    open suspend fun appendBlank(noteId: Note.Id): ThreadRecord {
        return boxStoreHolder.boxStore.awaitCallInTx {
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
        boxStoreHolder.boxStore.awaitCallInTx {
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