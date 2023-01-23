package net.pantasystem.milktea.data.infrastructure.notes.favorite

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.favorite.FavoriteRepository
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    val getAccount: GetAccount,
    val noteDataSourceAdder: NoteDataSourceAdder,
    private val favoriteAPIAdapter: FavoriteAPIAdapter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : FavoriteRepository {

    override suspend fun create(noteId: Note.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val account = getAccount.get(noteId.accountId)
                when(val result = favoriteAPIAdapter.create(noteId)) {
                    is SuccessfulResponseData.Mastodon -> {
                        noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, result.tootStatusDTO)
                    }
                    SuccessfulResponseData.Misskey -> Unit
                }
            }
        }
    }

    override suspend fun delete(noteId: Note.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val account = getAccount.get(noteId.accountId)
                when(val result = favoriteAPIAdapter.delete(noteId)) {
                    is SuccessfulResponseData.Mastodon -> {
                        noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, result.tootStatusDTO)
                    }
                    SuccessfulResponseData.Misskey -> Unit
                }
            }
        }
    }
}