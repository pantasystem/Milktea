package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.converters.FilePropertyDTOEntityConverter
import net.pantasystem.milktea.data.converters.NoteDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.NoteRelationEntities
import net.pantasystem.milktea.data.infrastructure.toEntities
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
    private val noteDTOEntityConverter: NoteDTOEntityConverter,
    private val filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
) {


    suspend fun addNoteDtoToDataSource(account: Account, noteDTO: NoteDTO, skipExists: Boolean = false): Note {
        val isMisskeyIo = account.getHost().lowercase() == "misskey.io"
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val entities =
            noteDTO.toEntities(
                account,
                nodeInfo,
                userDTOEntityConverter,
                noteDTOEntityConverter,
                filePropertyDTOEntityConverter
            )
        // TODO: misskeyの不整合問題が解決したらmisskey.ioの比較を削除する
        // TODO: misskey.ioのデータが信用できないので、キャッシュ上に存在する場合はスキップする
        if (skipExists || isMisskeyIo) {
            userDataSource.addAll(
                entities.users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            )
            noteDataSource.addAll(
                entities.notes.filterNot {
                    noteDataSource.exists(it.id)
                }
            )
            if (!noteDataSource.exists(entities.note.id)) {
                noteDataSource.add(entities.note)
            }
            filePropertyDataSource.addAll(
                entities.files.filterNot {
                    filePropertyDataSource.find(it.id).isSuccess
                }
            )
        } else {
            userDataSource.addAll(entities.users)
            noteDataSource.addAll(entities.notes)
            filePropertyDataSource.addAll(entities.files)
        }

        return entities.note
    }

    suspend fun addTootStatusDtoIntoDataSource(account: Account, status: TootStatusDTO, skipExists: Boolean = false): Note {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val entities = status.toEntities(account, nodeInfo)
        if (skipExists) {
            userDataSource.addAll(
                entities.users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            )
            noteDataSource.addAll(
                entities.notes.filterNot {
                    noteDataSource.exists(it.id)
                }
            )
            if (!noteDataSource.exists(entities.note.id)) {
                noteDataSource.add(entities.note)
            }
            filePropertyDataSource.addAll(
                entities.files.filterNot {
                    filePropertyDataSource.find(it.id).isSuccess
                }
            )
        } else {
            userDataSource.addAll(entities.users)
            noteDataSource.addAll(entities.notes)
            noteDataSource.add(entities.note)
            filePropertyDataSource.addAll(entities.files)
        }

        return entities.note
    }
}

suspend fun NoteDTO.toEntities(
    account: Account,
    nodeInfo: NodeInfo?,
    userDTOEntityConverter: UserDTOEntityConverter,
    noteDTOEntityConverter: NoteDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
): NoteRelationEntities {
    val dtoList = mutableListOf<NoteDTO>()
    dtoList.add(this)


    if (this.reply != null) {

        dtoList.add(this.reply!!)
    }
    if (this.reNote != null) {
        dtoList.add(reNote!!)
    }

    val note = noteDTOEntityConverter.convert(this, account, nodeInfo)
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()

    pickEntities(
        account,
        notes,
        users,
        files,
        nodeInfo,
        userDTOEntityConverter,
        noteDTOEntityConverter,
        filePropertyDTOEntityConverter
    )
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
    noteDTOEntityConverter: NoteDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
) {
    val (note, user) = this.toNoteAndUser(
        account,
        nodeInfo,
        userDTOEntityConverter,
        noteDTOEntityConverter
    )
    notes.add(note)
    users.add(user)
    files.addAll(
        this.files?.map {
            filePropertyDTOEntityConverter.convert(it, account)
        } ?: emptyList()
    )
    if (this.reply != null) {
        this.reply!!.pickEntities(
            account,
            notes,
            users,
            files,
            nodeInfo,
            userDTOEntityConverter,
            noteDTOEntityConverter,
            filePropertyDTOEntityConverter
        )
    }

    if (this.reNote != null) {
        this.reNote!!.pickEntities(
            account,
            notes,
            users,
            files,
            nodeInfo,
            userDTOEntityConverter,
            noteDTOEntityConverter,
            filePropertyDTOEntityConverter
        )
    }
}

suspend fun NoteDTO.toNoteAndUser(
    account: Account,
    nodeInfo: NodeInfo?,
    userDTOEntityConverter: UserDTOEntityConverter,
    noteDTOEntityConverter: NoteDTOEntityConverter,
): Pair<Note, User> {
    val note = noteDTOEntityConverter.convert(this, account, nodeInfo)
    val user = userDTOEntityConverter.convert(account, user, false)
    return note to user
}