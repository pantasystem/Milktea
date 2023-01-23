package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.notes.Note


interface ReactionHistoryDataSource {

    fun findAll(): Flow<List<ReactionHistory>>

    fun filter(noteId: Note.Id, type: String? = null): Flow<List<ReactionHistory>>



    suspend fun add(reactionHistory: ReactionHistory)

    suspend fun addAll(reactionHistories: List<ReactionHistory>)

    suspend fun clear(noteId: Note.Id)
}