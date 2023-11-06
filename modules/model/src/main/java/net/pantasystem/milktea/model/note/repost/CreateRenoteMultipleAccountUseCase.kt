package net.pantasystem.milktea.model.note.repost

import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.ap.ApResolverService
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRenoteMultipleAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val noteRepository: NoteRepository,
    private val checkCanRepostService: CheckCanRepostService,
    private val apResolverService: ApResolverService,
) : UseCase {

    /**
     * [targetNoteId]を持つノートをHTL/LTL/STL/GTLへリノートする。
     * チャンネル内に投稿されたノートの場合、チャンネル内にはリノートされず各種TLへリノートされる。
     * チャンネル内にリノートしたい場合は[renoteToChannel]を使用する。
     *
     * @param targetNoteId リノート対象の[Note.Id]
     * @param publisherAccountIds このアカウントからリノートを行う
     */
    suspend fun renote(
        targetNoteId: Note.Id,
        publisherAccountIds: List<Long>,
    ): Result<List<Result<Note>>> = runCancellableCatching {
        coroutineScope {
            val note = recursiveSearchHasContentNote(targetNoteId).getOrThrow()
            publisherAccountIds.map {
                resolveAndRenote(note, it) { a, n ->
                    CreateRenote.ofTimeline(a, n)
                }
            }
        }
    }

    /**
     * [targetNoteId]を持つノートが投稿されたチャンネルと同じチャンネルにリノートする。
     * [targetNoteId]がチャンネル外に投稿されたノートの場合、特定のチャンネル内にリノートということにはならず[renote]と同等の挙動となる。
     * チャンネル外にリノートしたい場合は[renote]を使用する。
     *
     * @param targetNoteId リノート対象の[Note.Id]
     * @param publisherAccountIds このアカウントからリノートを行う
     */
    suspend fun renoteToChannel(
        targetNoteId: Note.Id,
        publisherAccountIds: List<Long>,
    ): Result<List<Result<Note>>> = runCancellableCatching {
        coroutineScope {
            val note = recursiveSearchHasContentNote(targetNoteId).getOrThrow()
            publisherAccountIds.map {
                resolveAndRenote(note, it) { a, n ->
                    CreateRenote.ofChannel(a, n)
                }
            }
        }
    }

    private suspend fun resolveAndRenote(
        targetNote: Note,
        publisherAccountId: Long,
        paramFactory: (Account, Note) -> CreateRenote
    ): Result<Note> = runCancellableCatching {
        val publisherAccount = accountRepository.get(publisherAccountId).getOrThrow()
        val relatedTargetNoteAccount = accountRepository.get(targetNote.id.accountId).getOrThrow()

        val resolvedNote = if (publisherAccount.getHost() != relatedTargetNoteAccount.getHost()) {
            // リノート発信アカウントと対象ノートのホストが異なる場合、
            // AP経由でリノート発信アカウントのあるホストに対象ノートを取り寄せ、ホストに複製されたノートをリノートする
            apResolverService
                .resolve(targetNote.id, publisherAccountId)
                .getOrThrow()
        } else {
            noteRepository
                .find(Note.Id(publisherAccount.accountId, targetNote.id.noteId))
                .getOrThrow()
        }

        if (!checkCanRepostService.canRepost(resolvedNote.id).getOrElse { false }) {
            throw IllegalArgumentException()
        }

        val resolvedAccount = accountRepository.get(resolvedNote.id.accountId).getOrThrow()

        noteRepository
            .renote(paramFactory.invoke(resolvedAccount, resolvedNote))
            .getOrThrow()
    }

    private suspend fun recursiveSearchHasContentNote(noteId: Note.Id): Result<Note> =
        runCancellableCatching {
            val note = noteRepository.find(noteId).getOrThrow()
            if (note.hasContent()) {
                note
            } else {
                recursiveSearchHasContentNote(note.renoteId!!).getOrThrow()
            }
        }
}

