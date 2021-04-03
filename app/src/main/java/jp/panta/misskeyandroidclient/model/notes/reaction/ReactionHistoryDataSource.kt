package jp.panta.misskeyandroidclient.model.notes.reaction

import jp.panta.misskeyandroidclient.model.notes.Note
import kotlinx.coroutines.flow.Flow


interface ReactionHistoryDataSource {

    fun findAll(): Flow<List<ReactionHistory>>

    fun filterByNoteId(noteId: Note.Id): Flow<List<ReactionHistory>>

    fun filterByNoteIdAndType(noteId: Note.Id, type: String): Flow<List<ReactionHistory>>

    suspend fun add(reactionHistory: ReactionHistory)

    suspend fun addAll(reactionHistories: List<ReactionHistory>)
}