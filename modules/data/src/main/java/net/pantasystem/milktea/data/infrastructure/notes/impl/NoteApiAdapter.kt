package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.mastodon.status.CreateStatus
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.DeleteNote
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.notes.mute.ToggleThreadMuteRequest
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.drive.FileUploaderProvider
import net.pantasystem.milktea.data.infrastructure.drive.UploadSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.type4Mastodon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteApiAdapter @Inject constructor(
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val loggerFactory: Logger.Factory,
    val filePropertyDataSource: FilePropertyDataSource,
    private val uploader: FileUploaderProvider
) {

    suspend fun create(createNote: CreateNote): NoteCreatedResultType {
        return when (createNote.author.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val task = PostNoteTask(
                    createNote,
                    createNote.author,
                    loggerFactory,
                    filePropertyDataSource
                )
                val result = runCancellableCatching {
                    task.execute(
                        uploader.get(createNote.author)
                    ) ?: throw IllegalStateException("ファイルのアップロードに失敗しました")
                }.mapCancellableCatching {
                    misskeyAPIProvider.get(createNote.author).create(it).throwIfHasError()
                        .body()?.createdNote
                }.onFailure {
                    loggerFactory.create("NoteApiAdapter").error("create note error", it)
                }

                val noteDTO = result.getOrThrow()
                NoteResultType.Misskey(requireNotNull(noteDTO))
            }
            Account.InstanceType.MASTODON -> {
                val fileIds = coroutineScope {
                    createNote.files?.map { appFile ->
                        async {
                            when (appFile) {
                                is AppFile.Local -> {
                                    uploader.get(createNote.author)
                                        .upload(UploadSource.LocalFile(appFile), false).id
                                }
                                is AppFile.Remote -> appFile.id
                            }
                        }
                    }?.awaitAll()
                }
                val body = mastodonAPIProvider.get(createNote.author).createStatus(
                    CreateStatus(
                        status = createNote.text ?: "",
                        mediaIds = fileIds?.map {
                            it.fileId
                        } ?: emptyList(),
                        inReplyToId = createNote.replyId?.noteId,
                        spoilerText = createNote.cw,
                        sensitive = createNote.isSensitive ?: false,
                        visibility = createNote.visibility.type4Mastodon(),
                        poll = createNote.poll?.let { poll ->
                            CreateStatus.CreatePoll(
                                options = poll.choices,
                                multiple = poll.multiple,
                                expiresIn = poll.expiresAt?.let {
                                    (it - Clock.System.now().toEpochMilliseconds()) / 1000
                                }?.toInt() ?: (5 * 60),
                            )
                        },
                    )
                ).throwIfHasError().body()
                NoteResultType.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun showNote(noteId: Note.Id): ShowNoteResultType {
        val account = accountRepository.get(noteId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val body = misskeyAPIProvider.get(account).showNote(
                    NoteRequest(
                        i = account.token,
                        noteId = noteId.noteId
                    )
                ).throwIfHasError().body()
                NoteResultType.Misskey(requireNotNull(body))
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account)
                    .getStatus(noteId.noteId)
                    .throwIfHasError().body()
                NoteResultType.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun delete(noteId: Note.Id) {
        val account = accountRepository.get(noteId.accountId).getOrThrow()
        when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).delete(
                    DeleteNote(
                        i = account.token,
                        noteId = noteId.noteId
                    )
                ).throwIfHasError()
            }
            Account.InstanceType.MASTODON -> {
                mastodonAPIProvider.get(account).deleteStatus(noteId.noteId)
                    .throwIfHasError()
            }
        }
    }

    suspend fun createThreadMute(noteId: Note.Id): ToggleThreadMuteResultType {
        val account = accountRepository.get(noteId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).createThreadMute(
                    ToggleThreadMuteRequest(
                        i = account.token,
                        noteId = noteId.noteId
                    )
                ).throwIfHasError()
                ToggleThreadMuteResultType.Misskey
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account)
                    .muteConversation(noteId.noteId)
                    .throwIfHasError()
                    .body()
                ToggleThreadMuteResultType.Mastodon(requireNotNull(body))
            }
        }
    }

    suspend fun deleteThreadMute(noteId: Note.Id): ToggleThreadMuteResultType {
        val account = accountRepository.get(noteId.accountId).getOrThrow()
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                misskeyAPIProvider.get(account).deleteThreadMute(
                    ToggleThreadMuteRequest(
                        i = account.token,
                        noteId = noteId.noteId,
                    )
                ).throwIfHasError()
                ToggleThreadMuteResultType.Misskey
            }
            Account.InstanceType.MASTODON -> {
                val body = mastodonAPIProvider.get(account).unmuteConversation(noteId.noteId)
                    .throwIfHasError()
                    .body()
                ToggleThreadMuteResultType.Mastodon(requireNotNull(body))
            }
        }
    }

}

sealed interface NoteResultType {
    data class Misskey(val note: NoteDTO) : NoteResultType
    data class Mastodon(val status: TootStatusDTO) : NoteResultType
}

typealias ShowNoteResultType = NoteResultType

typealias NoteCreatedResultType = NoteResultType

sealed interface ToggleThreadMuteResultType {
    object Misskey : ToggleThreadMuteResultType
    data class Mastodon(val status: TootStatusDTO) : ToggleThreadMuteResultType
}