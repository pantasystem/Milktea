package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
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
        usersMap: Map<User.Id, User> = emptyMap(),
        notesMap: Map<Note.Id, Note> = emptyMap(),
    ): Result<NoteRelation?> {
        return runCancellableCatching {
            notesMap.getOrElse(noteId) {
                noteRepository.find(noteId).getOrThrow()
            }
        }.mapCancellableCatching {
            get(
                it,
                deep,
                usersMap = usersMap,
                notesMap = notesMap
            ).getOrThrow()
        }
    }

    suspend fun getIn(noteIds: List<Note.Id>): List<NoteRelation> {
        val notes = noteRepository.findIn(noteIds)
        val acIdAndUserIdMap = notes.map {
            it.userId
        }.groupBy {
            it.accountId
        }
        val users = acIdAndUserIdMap.map { list ->
            userDataSource.getIn(list.key, list.value.map { it.id }).getOrElse { emptyList() }
        }.flatten().associateBy {
            it.id
        }
        val noteMap = notes.associateBy { it.id }
        return notes.mapNotNull {
            get(
                it,
                true,
                usersMap = users,
                notesMap = noteMap
            ).getOrNull()
        }
    }

    suspend fun get(
        accountId: Long,
        noteId: String,
    ): Result<NoteRelation?> {
        return get(Note.Id(accountId, noteId))
    }


    suspend fun get(
        note: Note,
        deep: Boolean = true,
        usersMap: Map<User.Id, User> = emptyMap(),
        notesMap: Map<Note.Id, Note> = emptyMap(),
    ): Result<NoteRelation> {
        return runCancellableCatching {
            val user = usersMap.getOrElse(note.userId) {
                userDataSource.get(note.userId).getOrThrow()
            }

            val renote = if (deep) {
                note.renoteId?.let {
                    get(it, note.isRenote())
                }
            } else null
            val reply = if (deep) {
                note.replyId?.let {
                    get(it, false, notesMap = notesMap, usersMap = usersMap)
                }
            } else null

            return@runCancellableCatching NoteRelation.Normal(
                note = note,
                user = user,
                renote = renote?.getOrNull(),
                reply = reply?.getOrNull(),
                note.fileIds?.let { filePropertyDataSource.findIn(it).getOrNull() },
            )
        }

    }
}