package net.pantasystem.milktea.data.infrastructure.clip

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.clip.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.ClipDTOEntityConverter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.clip.*
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class ClipRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val clipDTOEntityConverter: ClipDTOEntityConverter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ClipRepository {

    override suspend fun getMyClips(accountId: Long): Result<List<Clip>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            val body = api.findMyClips(I(account.token)).throwIfHasError().body()
            requireNotNull(body).map {
                clipDTOEntityConverter.convert(account, it)
            }
        }
    }

    override suspend fun findOne(clipId: ClipId): Result<Clip> = runCancellableCatching  {
        withContext(ioDispatcher) {
            val account = accountRepository.get(clipId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            val body = api.showClip(ShowClipRequest(i = account.token, clipId = clipId.clipId))
                .throwIfHasError()
                .body()
            clipDTOEntityConverter.convert(account, requireNotNull(body))
        }
    }

    override suspend fun findBy(
        userId: User.Id,
        sinceId: String?,
        untilId: String?,
        limit: Int
    ): Result<List<Clip>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            val body = api.findByUsersClip(
                FindUsersClipRequest(
                    i = account.token,
                    userId = userId.id,
                    limit = limit,
                    sinceId = sinceId,
                    untilId = untilId
                )
            ).throwIfHasError().body()
            requireNotNull(body).map {
                clipDTOEntityConverter.convert(account, it)
            }
        }
    }

    override suspend fun findBy(
        noteId: Note.Id
    ): Result<List<Clip>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(noteId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            val body = api.findByNotesClip(FindNotesClip(
                i = account.token,
                noteId = noteId.noteId,
            )).throwIfHasError().body()
            requireNotNull(body).map {
                clipDTOEntityConverter.convert(account, it)
            }
        }
    }

    override suspend fun create(
        createClip: CreateClip
    ): Result<Clip> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(createClip.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            val body = api.createClip(CreateClipRequest(
                i = account.token,
                name = createClip.name,
                description = createClip.description,
                isPublic = createClip.isPublic
            )).throwIfHasError().body()
            clipDTOEntityConverter.convert(account, requireNotNull(body))
        }
    }

    override suspend fun update(
        clipId: ClipId,
        updateClip: UpdateClip
    ): Result<Clip> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(clipId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            val body = api.updateClip(UpdateClipRequest(
                i = account.token,
                name = updateClip.name,
                description = updateClip.description,
                isPublic = updateClip.isPublic,
                clipId = clipId.clipId
            )).throwIfHasError().body()
            clipDTOEntityConverter.convert(account, requireNotNull(body))
        }
    }

    override suspend fun delete(
        clipId: ClipId
    ): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(clipId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            api.deleteClip(DeleteClipRequest(i = account.token, clipId = clipId.clipId))
                .throwIfHasError()
        }
    }

    override suspend fun appendNote(
        clipId: ClipId,
        noteId: Note.Id
    ): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(clipId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            api.addNoteToClip(AddNoteToClipRequest(
                i = account.token,
                noteId = noteId.noteId,
                clipId = clipId.clipId
            )).throwIfHasError()
        }
    }

    override suspend fun removeNote(
        clipId: ClipId,
        noteId: Note.Id
    ): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = accountRepository.get(clipId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account)
            api.removeNoteToClip(RemoveNoteToClipRequest(
                i = account.token,
                noteId = noteId.noteId,
                clipId = clipId.clipId
            )).throwIfHasError()
        }
    }

}