package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.notes.reaction.CreateReaction

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Boolean

    suspend fun create(createNote: CreateNote): Note

    suspend fun find(noteId: Note.Id): Note

    /**
     * @param createReaction リアクションを作成するための値を表す。
     * リアクションに成功するとtrueが返される。
     * 既に選択されているリアクションが選択されている時はtoggleされる。
     * 既に選択されているリアクションとは異なるリアクションを選択した時は解除してリアクションが作成される。
     */
    @Deprecated("UseCase層の責務なので切り出す")
    suspend fun toggleReaction(createReaction: CreateReaction): Boolean

    suspend fun reaction(createReaction: CreateReaction): Boolean

    suspend fun unreaction(noteId: Note.Id): Boolean
}