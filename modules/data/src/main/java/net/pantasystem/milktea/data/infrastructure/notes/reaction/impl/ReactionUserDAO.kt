package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.model.note.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReactionUserDAO @Inject constructor(
    private val boxStore: BoxStore,
) {

    private val reactionBox: Box<ReactionUsersRecord> by lazy {
        boxStore.boxFor()
    }

    fun findBy(noteId: Note.Id, reaction: String?): ReactionUsersRecord? {
        return reactionBox.query().equal(
            ReactionUsersRecord_.accountIdAndNoteIdAndReaction,
            ReactionUsersRecord.generateUniqueId(noteId, reaction),
            QueryBuilder.StringOrder.CASE_SENSITIVE
        ).build().findFirst()
    }

    fun update(noteId: Note.Id, reaction: String?, accountIds: List<String>) {
        val record = createEmptyIfNotExists(noteId, reaction)
        record.accountIds = accountIds.toMutableList()
        reactionBox.put(record)
    }

    fun appendAccountIds(noteId: Note.Id, reaction: String?, accountIds: List<String>) {
        val record = createEmptyIfNotExists(noteId, reaction)
        record.accountIds.addAll(accountIds)
        reactionBox.put(record)
    }

    fun remove(noteId: Note.Id, reaction: String?) {
        findBy(noteId, reaction)?.let {
            reactionBox.remove(it)
        }
    }

    fun createEmptyIfNotExists(noteId: Note.Id, reaction: String?): ReactionUsersRecord {
        return when (val exists = findBy(noteId, reaction)) {
            null -> {
                reactionBox.put(
                    ReactionUsersRecord(
                        accountId = noteId.accountId,
                        noteId = noteId.noteId,
                        accountIdAndNoteIdAndReaction = ReactionUsersRecord.generateUniqueId(
                            noteId,
                            reaction
                        ),
                        reaction = reaction ?: "",
                    )
                )
                requireNotNull(findBy(noteId, reaction))
            }
            else -> exists
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBy(noteId: Note.Id, reaction: String?): Flow<ReactionUsersRecord?> {
        return reactionBox.query()
            .equal(
                ReactionUsersRecord_.accountIdAndNoteIdAndReaction,
                ReactionUsersRecord.generateUniqueId(noteId, reaction),
                QueryBuilder.StringOrder.CASE_SENSITIVE
            )
            .build()
            .subscribe()
            .toFlow().map {
                it.firstOrNull()
            }
    }

}