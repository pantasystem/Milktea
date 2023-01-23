package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.CreateReactionDTO
import net.pantasystem.milktea.api.misskey.notes.DeleteNote
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIWithAccountProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.onIReacted
import net.pantasystem.milktea.data.infrastructure.notes.onIUnReacted
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.CreateReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionRepository
import javax.inject.Inject

class ReactionRepositoryImpl @Inject constructor(
    private val getAccount: GetAccount,
    private val noteCaptureAPIProvider: NoteCaptureAPIWithAccountProvider,
    private val noteRepository: NoteRepository,
    private val noteDataSource: NoteDataSource,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val nodeInfoRepository: NodeInfoRepository,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ReactionRepository {

    override suspend fun create(createReaction: CreateReaction): Result<Boolean> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                val account = getAccount.get(createReaction.noteId.accountId)
                val note = noteRepository.find(createReaction.noteId).getOrThrow()

                runCancellableCatching {
                    when (account.instanceType) {
                        Account.InstanceType.MISSKEY -> {
                            if (postReaction(createReaction) && noteCaptureAPIProvider.get(account)
                                    ?.isCaptured(createReaction.noteId.noteId) == false
                            ) {
                                noteDataSource.add(note.onIReacted(createReaction.reaction))
                            }
                        }
                        Account.InstanceType.MASTODON -> {
                            if (nodeInfoRepository.find(account.getHost())
                                    .getOrThrow().type !is NodeInfo.SoftwareType.Mastodon.Fedibird
                            ) {
                                throw IllegalArgumentException("Mastodon is not support reaction, host:${account.getHost()}, username:${account.userName}")
                            }
                            val body = mastodonAPIProvider.get(account).reaction(
                                createReaction.noteId.noteId,
                                Reaction(createReaction.reaction).getNameAndHost()
                            ).throwIfHasError().body()
                            noteDataSourceAdder.addTootStatusDtoIntoDataSource(
                                account,
                                requireNotNull(body)
                            )
                        }
                    }

                    true
                }.getOrElse { e ->
                    if (e is APIError.ClientException) {
                        return@getOrElse false
                    }
                    throw e
                }
            }
        }

    override suspend fun delete(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        withContext(ioDispatcher) {
            val note = noteRepository.find(noteId).getOrThrow()
            val account = getAccount.get(noteId.accountId)
            when (account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    postUnReaction(noteId)
                            && (noteCaptureAPIProvider.get(account)
                        ?.isCaptured(noteId.noteId) == true
                            || (note.myReaction != null
                            && noteDataSource.add(note.onIUnReacted())
                        .getOrThrow() != AddResult.Canceled))
                }
                Account.InstanceType.MASTODON -> {
                    if (nodeInfoRepository.find(account.getHost())
                            .getOrThrow().type !is NodeInfo.SoftwareType.Mastodon.Fedibird
                    ) {
                        return@withContext false
                    }
                    val res = mastodonAPIProvider.get(account)
                        .unreaction(noteId.noteId)
                        .throwIfHasError()
                        .body()
                    noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, requireNotNull(res))
                    true
                }
            }

        }
    }


    private suspend fun postReaction(createReaction: CreateReaction): Boolean {
        val account = getAccount.get(createReaction.noteId.accountId)
        val res = misskeyAPIProvider.get(account).createReaction(
            CreateReactionDTO(
                i = account.token,
                noteId = createReaction.noteId.noteId,
                reaction = createReaction.reaction
            )
        )
        res.throwIfHasError()
        return res.isSuccessful
    }

    private suspend fun postUnReaction(noteId: Note.Id): Boolean {
        val note = noteRepository.find(noteId).getOrThrow()
        val account = getAccount.get(noteId.accountId)
        val res = misskeyAPIProvider.get(account).deleteReaction(
            DeleteNote(
                noteId = note.id.noteId,
                i = account.token
            )
        )
        res.throwIfHasError()
        return res.isSuccessful

    }
}