package net.pantasystem.milktea.model.notes.reaction

interface ReactionRepository {

    suspend fun create(createReaction: CreateReaction): Result<Boolean>
    suspend fun delete(deleteReaction: DeleteReaction): Result<Boolean>

}