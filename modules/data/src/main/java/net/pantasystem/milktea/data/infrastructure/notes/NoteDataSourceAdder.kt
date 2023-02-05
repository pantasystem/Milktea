package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.NoteRelationEntities
import net.pantasystem.milktea.data.infrastructure.toEntities
import net.pantasystem.milktea.data.infrastructure.toFileProperty
import net.pantasystem.milktea.data.infrastructure.toNote
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteDataSourceAdder @Inject constructor(
    private val userDataSource: UserDataSource,
    private val noteDataSource: NoteDataSource,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val nodeInfoRepository: NodeInfoRepository,
    private val userDTOEntityConverter: UserDTOEntityConverter,
) {


    suspend fun addNoteDtoToDataSource(account: Account, noteDTO: NoteDTO): Note {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val entities = noteDTO.toEntities(account, nodeInfo, userDTOEntityConverter)
        userDataSource.addAll(entities.users)
        noteDataSource.addAll(entities.notes)
        filePropertyDataSource.addAll(entities.files)
        return entities.note
    }

    suspend fun addTootStatusDtoIntoDataSource(account: Account, status: TootStatusDTO): Note {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val entities = status.toEntities(account, nodeInfo)
        userDataSource.addAll(entities.users)
        noteDataSource.addAll(entities.notes)
        noteDataSource.add(entities.note)
        filePropertyDataSource.addAll(entities.files)
        return entities.note
    }
}

suspend fun NoteDTO.toEntities(
    account: Account,
    nodeInfo: NodeInfo?,
    userDTOEntityConverter: UserDTOEntityConverter
): NoteRelationEntities {
    val dtoList = mutableListOf<NoteDTO>()
    dtoList.add(this)


    if (this.reply != null) {

        dtoList.add(this.reply!!)
    }
    if (this.reNote != null) {
        dtoList.add(reNote!!)
    }

    val note = this.toNote(account, nodeInfo)
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()

    pickEntities(account, notes, users, files, nodeInfo, userDTOEntityConverter)
    return NoteRelationEntities(
        note = note,
        notes = notes,
        users = users,
        files = files
    )
}

private suspend fun NoteDTO.pickEntities(
    account: Account,
    notes: MutableList<Note>,
    users: MutableList<User>,
    files: MutableList<FileProperty>,
    nodeInfo: NodeInfo?,
    userDTOEntityConverter: UserDTOEntityConverter,
) {
    val (note, user) = this.toNoteAndUser(account, nodeInfo, userDTOEntityConverter)
    notes.add(note)
    users.add(user)
    files.addAll(
        this.files?.map {
            it.toFileProperty(account)
        } ?: emptyList()
    )
    if (this.reply != null) {
        this.reply!!.pickEntities(account, notes, users, files, nodeInfo, userDTOEntityConverter)
    }

    if (this.reNote != null) {
        this.reNote!!.pickEntities(account, notes, users, files, nodeInfo, userDTOEntityConverter)
    }
}

suspend fun NoteDTO.toNoteAndUser(
    account: Account,
    nodeInfo: NodeInfo?,
    userDTOEntityConverter: UserDTOEntityConverter
): Pair<Note, User> {
    val note = this.toNote(account, nodeInfo)
    val user = userDTOEntityConverter.convert(account, user, false)
    return note to user
}