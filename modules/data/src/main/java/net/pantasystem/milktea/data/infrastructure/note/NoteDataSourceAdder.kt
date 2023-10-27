package net.pantasystem.milktea.data.infrastructure.note

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.converters.FilePropertyDTOEntityConverter
import net.pantasystem.milktea.data.converters.NoteDTOEntityConverter
import net.pantasystem.milktea.data.converters.TootDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.NoteRelationEntities
import net.pantasystem.milktea.data.infrastructure.toEntities
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteDataSourceAdder @Inject constructor(
    private val userDataSource: UserDataSource,
    private val noteDataSource: NoteDataSource,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val noteDTOEntityConverter: NoteDTOEntityConverter,
    private val filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
    private val tootDTOEntityConverter: TootDTOEntityConverter,
    private val instanceInfoService: InstanceInfoService,
    private val loggerFactory: Logger.Factory
) {

    private val logger by lazy {
        loggerFactory.create("NoteDataSourceAdder")
    }


    suspend fun addNoteDtoListToDataSource(
        account: Account,
        noteDTOs: List<NoteDTO>,
        skipExists: Boolean = false,
        instanceType: InstanceInfoType? = null
    ): List<Note> {
        val info = instanceType?.takeIf {
            it.uri == account.normalizedInstanceUri
        } ?: instanceInfoService.find(account.normalizedInstanceUri).getOrNull()
        val entities = noteDTOs.map {
            it.toEntities(
                account,
                userDTOEntityConverter,
                noteDTOEntityConverter,
                filePropertyDTOEntityConverter,
                info,
            )

        }
        if (skipExists) {
            userDataSource.addAll(
                entities.flatMap {
                    it.users
                }.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            ).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                entities.flatMap {
                    it.notes
                }.filterNot {
                    noteDataSource.exists(it.id)
                }
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(
                entities.flatMap {
                    it.files
                }.filterNot {
                    filePropertyDataSource.find(it.id).isSuccess
                }
            ).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        } else {
            userDataSource.addAll(entities.flatMap {
                it.users
            }).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(entities.flatMap {
                it.notes
            }).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(entities.flatMap {
                it.files
            }).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }
        return entities.map {
            it.note
        }
    }

    suspend fun addNoteDtoToDataSource(
        account: Account,
        noteDTO: NoteDTO,
        skipExists: Boolean = false,
        instanceType: InstanceInfoType? = null
    ): Note {
        val info = instanceType?.takeIf {
            it.uri == account.normalizedInstanceUri
        } ?: instanceInfoService.find(account.normalizedInstanceUri).getOrNull()
        val entities =
            noteDTO.toEntities(
                account,
                userDTOEntityConverter,
                noteDTOEntityConverter,
                filePropertyDTOEntityConverter,
                info,
            )
        if (skipExists) {
            userDataSource.addAll(
                entities.users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            ).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                entities.notes.filterNot {
                    noteDataSource.exists(it.id)
                }
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            if (!noteDataSource.exists(entities.note.id)) {
                noteDataSource.add(entities.note).onFailure {
                    logger.error("NoteDataSourceへの追加に失敗", it)
                }
            }
            filePropertyDataSource.addAll(
                entities.files.filterNot {
                    filePropertyDataSource.find(it.id).isSuccess
                }
            ).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        } else {
            userDataSource.addAll(entities.users).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(entities.notes).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(entities.files).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }

        return entities.note
    }

    suspend fun addTootStatusDtoIntoDataSource(
        account: Account,
        status: TootStatusDTO,
        skipExists: Boolean = false
    ): Note {
        val entities = status.toEntities(tootDTOEntityConverter, account)
        if (skipExists) {
            userDataSource.addAll(
                entities.users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            ).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                entities.notes.filterNot {
                    noteDataSource.exists(it.id)
                }
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            if (!noteDataSource.exists(entities.note.id)) {
                noteDataSource.add(entities.note).onFailure {
                    logger.error("NoteDataSourceへの追加に失敗", it)
                }
            }
            filePropertyDataSource.addAll(
                entities.files.filterNot {
                    filePropertyDataSource.find(it.id).isSuccess
                }
            ).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        } else {
            userDataSource.addAll(entities.users).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(entities.notes).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            noteDataSource.add(entities.note).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(entities.files).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }

        return entities.note
    }
}

suspend fun NoteDTO.toEntities(
    account: Account,
    userDTOEntityConverter: UserDTOEntityConverter,
    noteDTOEntityConverter: NoteDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
    instanceType: InstanceInfoType?,
): NoteRelationEntities {
    val dtoList = mutableListOf<NoteDTO>()
    dtoList.add(this)


    if (this.reply != null) {

        dtoList.add(this.reply!!)
    }
    if (this.reNote != null) {
        dtoList.add(reNote!!)
    }

    val note = noteDTOEntityConverter.convert(account, this, instanceType)
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()

    pickEntities(
        account,
        notes,
        users,
        files,
        userDTOEntityConverter,
        noteDTOEntityConverter,
        filePropertyDTOEntityConverter,
        instanceType,
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
    userDTOEntityConverter: UserDTOEntityConverter,
    noteDTOEntityConverter: NoteDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
    instanceType: InstanceInfoType?,
) {
    val (note, user) = this.toNoteAndUser(
        account,
        userDTOEntityConverter,
        noteDTOEntityConverter,
        instanceType,
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
            userDTOEntityConverter,
            noteDTOEntityConverter,
            filePropertyDTOEntityConverter,
            instanceType,
        )
    }

    if (this.reNote != null) {
        this.reNote!!.pickEntities(
            account,
            notes,
            users,
            files,
            userDTOEntityConverter,
            noteDTOEntityConverter,
            filePropertyDTOEntityConverter,
            instanceType,
        )
    }
}

suspend fun NoteDTO.toNoteAndUser(
    account: Account,
    userDTOEntityConverter: UserDTOEntityConverter,
    noteDTOEntityConverter: NoteDTOEntityConverter,
    instanceType: InstanceInfoType?,
): Pair<Note, User> {
    val note = noteDTOEntityConverter.convert(account, this, instanceType)
    val user = userDTOEntityConverter.convert(account, user, false)
    return note to user
}

