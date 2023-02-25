package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteEditorSwitchAccountExecutor @Inject constructor(
    private val resolverRepository: ApResolverRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val accountRepository: AccountRepository,
) {

    suspend operator fun invoke(
        fromAccount: Account?,
        sendToState: NoteEditorSendToState,
        switchToAccount: Account
    ): NoteEditorSwitchAccountExecutorResult {
        var convertTo = NoteEditorSwitchAccountExecutorResult(
            replyId = sendToState.replyId,
            renoteId = sendToState.renoteId,
            visibility = sendToState.visibility,
            channelId = sendToState.channelId
        )


        if (convertTo.replyId != null) {
            convertTo = resolveReply(fromAccount, convertTo, switchToAccount).getOrElse {
                convertTo.copy(replyId = null)
            }
        }

        if (convertTo.renoteId != null) {
            convertTo = resolveRenote(fromAccount, convertTo, switchToAccount).getOrElse {
                convertTo.copy(renoteId = null)
            }
        }

        if (convertTo.visibility is Visibility.Specified) {
            convertTo =
                resolveVisibility(convertTo, from = fromAccount, to = switchToAccount).getOrElse {
                    convertTo.copy(visibility = Visibility.Specified(emptyList()))
                }
        }

        if (convertTo.channelId != null) {
            val ac = accountRepository.get(convertTo.channelId!!.accountId).getOrElse {
                fromAccount
            }
            if (ac?.getHost() != switchToAccount.getHost()) {
                convertTo = convertTo.copy(
                    channelId = null,
                )
            }
        }

        return convertTo
    }

    private suspend fun resolveRenote(
        fromAccount: Account?,
        noteEditingState: NoteEditorSwitchAccountExecutorResult,
        account: Account
    ): Result<NoteEditorSwitchAccountExecutorResult> = runCancellableCatching {
        if (noteEditingState.renoteId != null) {
            val renote = noteRepository.find(noteEditingState.renoteId).getOrThrow()
            val url = renote.url ?: renote.uri ?: "${fromAccount?.normalizedInstanceDomain}/notes/${renote.id.noteId}"
            val toRenoteNote = resolverRepository.resolve(account.accountId, url)
                .getOrThrow() as ApResolver.TypeNote
            noteEditingState.copy(renoteId = toRenoteNote.note.id)
        } else {
            noteEditingState
        }
    }

    private suspend fun resolveReply(
        fromAccount: Account?,
        noteEditingState: NoteEditorSwitchAccountExecutorResult,
        account: Account
    ): Result<NoteEditorSwitchAccountExecutorResult> = runCancellableCatching {
        if (noteEditingState.replyId != null) {
            val reply = noteRepository.find(noteEditingState.replyId).getOrThrow()
            val url = reply.url ?: reply.uri ?: "${fromAccount?.normalizedInstanceDomain}/notes/${reply.id.noteId}"
            val toReplyNote = resolverRepository.resolve(account.accountId, url)
                .getOrThrow() as ApResolver.TypeNote
            noteEditingState.copy(replyId = toReplyNote.note.id)
        } else {
            noteEditingState
        }
    }

    private suspend fun resolveVisibility(
        noteEditingState: NoteEditorSwitchAccountExecutorResult,
        from: Account?,
        to: Account
    ): Result<NoteEditorSwitchAccountExecutorResult> = runCancellableCatching {
        coroutineScope {
            when (val visibility = noteEditingState.visibility) {
                is Visibility.Specified -> {
                    val userIds = visibility.visibleUserIds
                    if (userIds.isNotEmpty()) {
                        userRepository.syncIn(userIds)

                    }
                    val fromUsers =
                        userDataSource.getIn(from!!.accountId, userIds.map { it.id }).getOrThrow()
                    val toUsers = fromUsers.map {
                        async {
                            userRepository.findByUserName(
                                to.accountId,
                                userName = it.userName,
                                host = it.host
                            )
                        }
                    }.awaitAll()

                    noteEditingState.copy(visibility = Visibility.Specified(
                        toUsers.map { it.id }
                    ))
                }
                else -> noteEditingState
            }
        }

    }


}

data class NoteEditorSwitchAccountExecutorResult(
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val visibility: Visibility,
    val channelId: Channel.Id?,
)