package net.pantasystem.milktea.data.infrastructure.notes.bookmark

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.bookmark.BookmarkRepository
import net.pantasystem.milktea.model.notes.favorite.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val accountRepository: AccountRepository,
    private val favoriteRepository: FavoriteRepository,
    private val mastodonAPIProvider: MastodonAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
): BookmarkRepository {

    override suspend fun create(noteId: Note.Id): Result<Unit> = runCancellableCatching{
        withContext(ioDispatcher) {
            val account = accountRepository.get(noteId.accountId).getOrThrow()
            when(account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> favoriteRepository.create(noteId).getOrThrow()
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val body = mastodonAPIProvider.get(account).bookmarkStatus(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(body))
                }
            }
        }
    }

    override suspend fun delete(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(noteId.accountId).getOrThrow()
            when(account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> favoriteRepository.delete(noteId).getOrThrow()
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val body = mastodonAPIProvider.get(account).unbookmarkStatus(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(body))
                }
            }
        }
    }
}