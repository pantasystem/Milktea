package jp.panta.misskeyandroidclient.model.reaction.impl

import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.reaction.ReactionHistory
import jp.panta.misskeyandroidclient.model.reaction.ReactionHistoryDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryReactionHistoryDataSource : ReactionHistoryDataSource {

    private val lock = Mutex()
    private val stateFlow = MutableStateFlow(emptyList<ReactionHistory>())

    override suspend fun add(reactionHistory: ReactionHistory) {
        lock.withLock {
            stateFlow.value = stateFlow.value.toMutableList().also {
                it.add(reactionHistory)
            }
        }
    }

    override suspend fun addAll(reactionHistories: List<ReactionHistory>) {
        lock.withLock {
            stateFlow.value = stateFlow.value.toMutableList().also {
                it.addAll(reactionHistories)
            }
        }
    }

    override fun filterByNoteId(noteId: Note.Id): Flow<List<ReactionHistory>> {
        return stateFlow.map {
            it.filter { history ->
                history.id.accountId == noteId.accountId && noteId== history.noteId
            }.sortedBy { history ->
                history.id.reactionId
            }
        }
    }

    override fun filterByNoteIdAndType(noteId: Note.Id, type: String): Flow<List<ReactionHistory>> {
        return stateFlow.map {
            it.filter { history ->
                history.noteId == noteId
                        && history.id.accountId == noteId.accountId
                        && history.type == type
            }.sortedBy { history ->
                history.id.reactionId
            }
        }
    }

    override fun findAll(): Flow<List<ReactionHistory>> {
        return stateFlow
    }
}