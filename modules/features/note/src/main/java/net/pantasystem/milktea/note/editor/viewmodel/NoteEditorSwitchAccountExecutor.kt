package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.app_store.notes.NoteEditingState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteEditorSwitchAccountExecutor  @Inject constructor(
    private val resolverRepository: ApResolverRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
){

    suspend operator fun invoke(noteEditingState: NoteEditingState, switchToAccount: Account): Result<NoteEditingState> = runCatching  {
        val fromAccount = noteEditingState.author
        if (fromAccount?.getHost() == switchToAccount.getHost()) {
            noteEditingState.setAccount(switchToAccount)
        }

        var convertTo = noteEditingState

        if (convertTo.replyId != null) {
            convertTo = resolveReply(convertTo, switchToAccount).getOrElse {
                convertTo.copy(replyId = null)
            }
        }

        if (convertTo.renoteId != null) {
            convertTo = resolveRenote(convertTo, switchToAccount).getOrElse {
                convertTo.copy(renoteId = null)
            }
        }

        if (convertTo.visibility is Visibility.Specified) {
            convertTo = resolveVisibility(convertTo, from = fromAccount, to = switchToAccount).getOrElse {
                convertTo.copy(visibility = Visibility.Specified(emptyList()))
            }
        }

        convertTo.setAccount(switchToAccount)
    }

    private suspend fun resolveRenote(noteEditingState: NoteEditingState, account: Account): Result<NoteEditingState> = runCatching {
        val fromAccount = noteEditingState.author
        if (noteEditingState.renoteId != null) {
            val renote = noteRepository.find(noteEditingState.renoteId!!).getOrThrow()
            val url = "${fromAccount?.instanceDomain}/notes/${renote.id.noteId}"
            val toRenoteNote = resolverRepository.resolve(account.accountId, url).getOrThrow() as ApResolver.TypeNote
            noteEditingState.copy(renoteId = toRenoteNote.note.id)
        } else {
            noteEditingState
        }
    }

    private suspend fun resolveReply(noteEditingState: NoteEditingState, account: Account): Result<NoteEditingState> = runCatching {
        val fromAccount = noteEditingState.author
        if (noteEditingState.replyId != null) {
            val reply = noteRepository.find(noteEditingState.replyId!!).getOrThrow()
            val url = "${fromAccount?.instanceDomain}/notes/${reply.id.noteId}"
            val toReplyNote = resolverRepository.resolve(account.accountId, url).getOrThrow() as ApResolver.TypeNote
            noteEditingState.copy(replyId = toReplyNote.note.id)
        } else {
            noteEditingState
        }
    }

    private suspend fun resolveVisibility(noteEditingState: NoteEditingState, from: Account?, to: Account): Result<NoteEditingState> = runCatching {
        coroutineScope {
            when(val visibility = noteEditingState.visibility) {
                is Visibility.Specified -> {
                    val userIds = visibility.visibleUserIds
                    if (userIds.isNotEmpty()) {
                        userRepository.syncIn(userIds)

                    }
                    val fromUsers = userDataSource.getIn(from!!.accountId, userIds.map { it.id }).getOrThrow()
                    val toUsers = fromUsers.map {
                        async {
                            userRepository.findByUserName(to.accountId, userName = it.userName, host = it.host)
                        }
                    }.awaitAll()

                    noteEditingState.setVisibility(
                        Visibility.Specified(
                            toUsers.map { it.id }
                        )
                    )
                }
                else -> noteEditingState
            }
        }

    }


}