package net.pantasystem.milktea.model.clip

import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User

interface ClipRepository {

    suspend fun getMyClips(accountId: Long): Result<List<Clip>>

    suspend fun findBy(
        userId: User.Id,
        sinceId: String? = null,
        untilId: String? = null,
        limit: Int = 10
    ): Result<List<Clip>>

    suspend fun findBy(noteId: Note.Id): Result<List<Clip>>

    suspend fun create(createClip: CreateClip): Result<Clip>

    suspend fun update(clipId: ClipId, updateClip: UpdateClip): Result<Clip>

    suspend fun delete(clipId: ClipId): Result<Unit>

    suspend fun appendNote(clipId: ClipId, noteId: Note.Id): Result<Unit>

    suspend fun removeNote(clipId: ClipId, noteId: Note.Id): Result<Unit>

    suspend fun findOne(clipId: ClipId): Result<Clip>
}