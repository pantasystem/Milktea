package net.pantasystem.milktea.model.note.reaction

interface ReactionRepository {

    suspend fun create(createReaction: CreateReaction): Result<Boolean>
    suspend fun delete(deleteReaction: DeleteReaction): Result<Boolean>

}