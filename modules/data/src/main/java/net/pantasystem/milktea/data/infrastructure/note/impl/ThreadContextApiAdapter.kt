package net.pantasystem.milktea.data.infrastructure.note.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.api.misskey.notes.GetNoteChildrenRequest
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.note.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import javax.inject.Inject

/**
 * このインターフェースは、ノートのスレッドを表す構造体のキャッシュと、API上のリソースを同期するための機能を提供します。
 */
interface ThreadContextApiAdapter {
    interface Factory {
        suspend fun create(account: Account): ThreadContextApiAdapter
    }
    suspend fun syncThreadContext(
        noteId: Note.Id,
    ): Result<Unit>
}

/**
 * このクラスは、ThreadContextApiAdapter.Factoryの実装クラスで、
 * 対応しているAPIに応じて対応しているThreadContextApiAdapterの実装クラスを生成します。
 */
class ThreadContextApiAdapterFactoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val noteDataSource: NoteDataSource,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val getAccount: GetAccount,
    private val mastodonAPIProvider: MastodonAPIProvider,
) : ThreadContextApiAdapter.Factory {
    override suspend fun create(account: Account): ThreadContextApiAdapter {
        when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                return ThreadContextApiAdapterMisskeyPattern(
                    getAccount = getAccount,
                    misskeyAPIProvider = misskeyAPIProvider,
                    noteDataSource = noteDataSource,
                    noteDataSourceAdder = noteDataSourceAdder,
                )
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                return ThreadContextApiAdapterMastodonPattern(
                    accountRepository = accountRepository,
                    mastodonAPIProvider = mastodonAPIProvider,
                    noteDataSource = noteDataSource,
                    noteDataSourceAdder = noteDataSourceAdder,
                )
            }
        }
    }
}


/**
 * このクラスは、Mastodon系のAPIに対応するThreadContextApiAdapterの実装クラスです。
 */
class ThreadContextApiAdapterMastodonPattern @Inject constructor(
    private val accountRepository: AccountRepository,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val noteDataSource: NoteDataSource,
    private val noteDataSourceAdder: NoteDataSourceAdder,
) : ThreadContextApiAdapter {
    override suspend fun syncThreadContext(noteId: Note.Id): Result<Unit> = runCancellableCatching{
        val account = accountRepository.get(noteId.accountId).getOrThrow()
        val body = requireNotNull(
            mastodonAPIProvider.get(account).getStatusesContext(noteId.noteId)
                .throwIfHasError()
                .body()
        )
        body.ancestors.map {
            noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, it)
        }

        body.descendants.map {
            noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, it)
        }
    }
}

/**
 * このクラスは、Misskey系のAPIに対応するThreadContextApiAdapterの実装クラスです。
 */
class ThreadContextApiAdapterMisskeyPattern @Inject constructor(
    private val getAccount: GetAccount,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val noteDataSource: NoteDataSource,
    private val misskeyAPIProvider: MisskeyAPIProvider,
) : ThreadContextApiAdapter {
    override suspend fun syncThreadContext(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        val account = getAccount.get(noteId.accountId)
        requireNotNull(
            misskeyAPIProvider.get(account).conversation(
                NoteRequest(
                    i = account.token,
                    noteId = noteId.noteId,
                )
            ).throwIfHasError().body()
        ).map {
            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
        }
        syncRecursiveThreadContext4Misskey(noteId, noteId)
    }

    private suspend fun syncRecursiveThreadContext4Misskey(
        targetNoteId: Note.Id,
        appendTo: Note.Id,
    ) {
        val account = getAccount.get(appendTo.accountId)
        val descendants = getMisskeyDescendants(targetNoteId).map {
            noteDataSourceAdder.addNoteDtoToDataSource(account, it)
        }
        coroutineScope {
            descendants.map { note ->
                async {
                    syncRecursiveThreadContext4Misskey(note.id, appendTo)
                }
            }.awaitAll()
        }
    }

    private suspend fun getMisskeyDescendants(targetNoteId: Note.Id): List<NoteDTO> {
        val account = getAccount.get(targetNoteId.accountId)
        return requireNotNull(
            misskeyAPIProvider.get(account).children(
                GetNoteChildrenRequest(
                    i = account.token,
                    noteId = targetNoteId.noteId,
                    limit = 30,
                    depth = 2,
                )
            ).throwIfHasError().body()
        )
    }
}