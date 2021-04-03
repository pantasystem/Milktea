package jp.panta.misskeyandroidclient.model.notes.reaction.impl

import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryReactionHistoryDataSource : ReactionHistoryDataSource {

    private val lock = Mutex()
    private val stateFlow = MutableStateFlow(mapOf<ReactionHistory.Id, ReactionHistory>())

    override suspend fun add(reactionHistory: ReactionHistory) {
        lock.withLock {
            stateFlow.value = stateFlow.value.toMutableMap().also {
                it[reactionHistory.id] = reactionHistory
            }
        }
    }

    override suspend fun addAll(reactionHistories: List<ReactionHistory>) {
        lock.withLock {
            stateFlow.value = stateFlow.value.toMutableMap().also {
                it.putAll(reactionHistories.map { r ->
                    r.id to r
                })
            }
        }
    }


    override fun filter(noteId: Note.Id, type: String?): Flow<List<ReactionHistory>> {
        return stateFlow.map {
            it.values.filter { history ->
                history.noteId == noteId
                        && history.id.accountId == noteId.accountId
                        && (type == null || type == history.type)
            }.sortedBy { history ->
                history.id.reactionId
            }.asReversed()
        }
    }

    override fun findAll(): Flow<List<ReactionHistory>> {
        return stateFlow.map {
            it.values.toList()
        }
    }

    override suspend fun clear(noteId: Note.Id) {
        lock.withLock {
            stateFlow.value = stateFlow.value.filterNot {
                it.value.noteId == noteId
            }
        }
    }
}