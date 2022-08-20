package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteRelationGetter @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userDataSource: UserDataSource,
    private val filePropertyDataSource: FilePropertyDataSource,
) {

    suspend fun get(
        noteId: Note.Id,
        deep: Boolean = true,
        featuredId: String? = null,
        promotionId: String? = null,
        usersMap: Map<User.Id, User> = emptyMap(),
        notesMap: Map<Note.Id, Note> = emptyMap(),
    ): Result<NoteRelation?> {
        return runCatching {
            notesMap.getOrElse(noteId) {
                noteRepository.find(noteId).getOrThrow()
            }
        }.mapCatching {
            get(
                it,
                deep,
                featuredId = featuredId,
                promotionId = promotionId,
                usersMap = usersMap,
                notesMap = notesMap
            ).getOrThrow()
        }
    }

    suspend fun getIn(noteIds: List<Note.Id>): List<NoteRelation> {
        val notes = noteRepository.findIn(noteIds)
        val acIdAndNoteIdMap = noteIds.groupBy {
            it.accountId
        }
        val users = acIdAndNoteIdMap.map { list ->
            userDataSource.getIn(list.key, list.value.map { it.noteId }).getOrThrow()
        }.flatten().associateBy {
            it.id
        }
        val noteMap = notes.associateBy { it.id }
        return notes.mapNotNull {
            get(
                it,
                true,
                featuredId = null,
                promotionId = null,
                usersMap = users,
                notesMap = noteMap
            ).getOrNull()
        }
    }

    suspend fun get(
        accountId: Long,
        noteId: String,
        featuredId: String? = null,
        promotionId: String? = null
    ): Result<NoteRelation?> {
        return get(Note.Id(accountId, noteId), featuredId = featuredId, promotionId = promotionId)
    }


    suspend fun get(
        note: Note,
        deep: Boolean = true,
        featuredId: String? = null,
        promotionId: String? = null,
        usersMap: Map<User.Id, User> = emptyMap(),
        notesMap: Map<Note.Id, Note> = emptyMap(),
    ): Result<NoteRelation> {
        return runCatching {
            val user = usersMap.getOrElse(note.userId) {
                userDataSource.get(note.userId).getOrThrow()
            }

            val renote = if (deep) {
                note.renoteId?.let {
                    get(it, false)
                }
            } else null
            val reply = if (deep) {
                note.replyId?.let {
                    get(it, false, notesMap = notesMap, usersMap = usersMap)
                }
            } else null

            if (featuredId != null) {
                return@runCatching NoteRelation.Featured(
                    note,
                    user,
                    renote?.getOrNull(),
                    reply?.getOrNull(),
                    note.fileIds?.let { filePropertyDataSource.findIn(it) },
                    featuredId
                )
            }

            if (promotionId != null) {
                return@runCatching NoteRelation.Promotion(
                    note,
                    user,
                    renote?.getOrNull(),
                    reply?.getOrNull(),
                    note.fileIds?.let { filePropertyDataSource.findIn(it) },
                    promotionId
                )
            }
            return@runCatching NoteRelation.Normal(
                note = note,
                user = user,
                renote = renote?.getOrNull(),
                reply = reply?.getOrNull(),
                note.fileIds?.let { filePropertyDataSource.findIn(it) },
            )
        }

    }
}