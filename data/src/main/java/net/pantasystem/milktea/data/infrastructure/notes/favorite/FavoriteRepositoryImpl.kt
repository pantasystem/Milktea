package net.pantasystem.milktea.data.infrastructure.notes.favorite

import net.pantasystem.milktea.api.misskey.notes.favorite.CreateFavorite
import net.pantasystem.milktea.api.misskey.notes.favorite.DeleteFavorite
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AuthById
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.favorite.FavoriteRepository
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val auth: AuthById,
    val getAccount: GetAccount,
) : FavoriteRepository {

    override suspend fun create(noteId: Note.Id): Result<Unit> {
        return runCatching {
            val token = auth.getToken(noteId.accountId)
                ?: throw UnauthorizedException()
            misskeyAPIProvider.get(getAccount.get(noteId.accountId))
                .createFavorite(CreateFavorite(i = token, noteId = noteId.noteId))
                .throwIfHasError()
        }
    }

    override suspend fun delete(noteId: Note.Id): Result<Unit> {
        return runCatching {
            val token = auth.getToken(noteId.accountId)
                ?: throw UnauthorizedException()
            misskeyAPIProvider.get(getAccount.get(noteId.accountId))
                .deleteFavorite(DeleteFavorite(i = token, noteId = noteId.noteId))
                .throwIfHasError()
        }
    }
}