package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.common.flatMapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteRelationGetter @Inject constructor(
    private val noteRepository: NoteRepository,
    private val noteDataSource: NoteDataSource,
    private val userDataSource: UserDataSource,
    private val filePropertyDataSource: FilePropertyDataSource,
) {

    suspend fun get(
        noteId: Note.Id,
        deep: Boolean = true,
        usersMap: Map<User.Id, User> = emptyMap(),
        notesMap: Map<Note.Id, Note> = emptyMap(),
        filesMap: Map<FileProperty.Id, FileProperty> = emptyMap(),
        skipIfNotExistsInCache: Boolean = false,
    ): Result<NoteRelation?> {
        return runCancellableCatching {
            notesMap.getOrElse(noteId) {
                if (skipIfNotExistsInCache) {
                    noteDataSource.get(noteId).getOrThrow()
                } else {
                    noteRepository.find(noteId).getOrThrow()
                }

            }
        }.flatMapCancellableCatching {
            get(
                it,
                deep,
                usersMap = usersMap,
                notesMap = notesMap,
                filesMap = filesMap,
            )
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
            userDataSource.getIn(list.key, list.value.map { it.id }, isSimple = true)
                .getOrElse { emptyList() }
        }.flatten().associateBy {
            it.id
        }
        val noteMap = notes.associateBy { it.id }
        val files = filePropertyDataSource.findIn(
            notes.mapNotNull { it.fileIds }.flatten()
        ).getOrElse {
            emptyList()
        }.let { files ->
            files.associateBy { it.id }
        }
        return notes.mapNotNull {
            get(
                it,
                true,
                usersMap = users,
                notesMap = noteMap,
                filesMap = files,
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
        filesMap: Map<FileProperty.Id, FileProperty> = emptyMap(),
    ): Result<NoteRelation> {
        return runCancellableCatching {
            val user = usersMap.getOrElse(note.userId) {
                userDataSource.get(note.userId, isSimple = true).getOrThrow()
            }

            val renote = if (deep) {
                note.renoteId?.let {
                    get(it, note.isRenote(), skipIfNotExistsInCache = true)
                }
            } else null
            val reply = if (deep) {
                note.replyId?.let {
                    get(
                        it,
                        false,
                        notesMap = notesMap,
                        usersMap = usersMap,
                        skipIfNotExistsInCache = true,
                        filesMap = filesMap,
                    )
                }
            } else null

            val filesInDb = note.fileIds?.filter {
                filesMap[it] == null
            }?.let {
                filePropertyDataSource.findIn(it).getOrElse {
                    emptyList()
                }
            }?.associateBy { it.id }

            return@runCancellableCatching NoteRelation(
                note = note,
                user = user,
                renote = renote?.getOrNull(),
                reply = reply?.getOrNull(),
                files = note.fileIds?.mapNotNull {
                    filesMap[it] ?: filesInDb?.get(it)
                } ?: emptyList(),
            )
        }

    }
}