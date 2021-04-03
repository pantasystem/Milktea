package jp.panta.misskeyandroidclient.model.notes.reaction

import jp.panta.misskeyandroidclient.model.notes.Note
import kotlinx.coroutines.flow.Flow


interface ReactionHistoryDataSource {

    fun findAll(): Flow<List<ReactionHistory>>

    fun filter(noteId: Note.Id, type: String? = null): Flow<List<ReactionHistory>>



    suspend fun add(reactionHistory: ReactionHistory)

    suspend fun addAll(reactionHistories: List<ReactionHistory>)

    suspend fun clear(noteId: Note.Id)
}