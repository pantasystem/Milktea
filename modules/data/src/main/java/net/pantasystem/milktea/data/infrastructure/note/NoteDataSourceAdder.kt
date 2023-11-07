package net.pantasystem.milktea.data.infrastructure.note

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.converters.FilePropertyDTOEntityConverter
import net.pantasystem.milktea.data.converters.NoteDTOEntityConverter
import net.pantasystem.milktea.data.converters.TootDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.NoteDTOUnpacked
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
    ): List<Note.Id> {
        val info = instanceType?.takeIf {
            it.uri == account.normalizedInstanceUri
        } ?: instanceInfoService.find(account.normalizedInstanceUri).getOrNull()
        val entities = noteDTOs.map {
            it.toEntities(
                account,
                userDTOEntityConverter,
                filePropertyDTOEntityConverter,
                info,
            )
        }
        val notes = entities.flatMap {
            it.notes
        } + entities.map {
            it.note
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
                noteDTOEntityConverter.convertAll(
                    account,
                    notes.filterNot {
                        noteDataSource.exists(Note.Id(account.accountId, it.id))
                    },
                    info,
                )
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
            noteDataSource.addAll(
                noteDTOEntityConverter.convertAll(
                    account,
                    notes,
                    info,
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(entities.flatMap {
                it.files
            }).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }
        return entities.map {
            Note.Id(account.accountId, it.note.id)
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
                filePropertyDTOEntityConverter,
                info,
            )
        val willReturnNote = noteDTOEntityConverter.convert(account, entities.note)
        if (skipExists) {
            userDataSource.addAll(
                entities.users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            ).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                noteDTOEntityConverter.convertAll(
                    account,
                    entities.notes.filterNot {
                        noteDataSource.exists(Note.Id(account.accountId, it.id))
                    }
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            if (!noteDataSource.exists(Note.Id(account.accountId, entities.note.id))) {
                noteDataSource.add(willReturnNote).onFailure {
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
            noteDataSource.addAll(
                noteDTOEntityConverter.convertAll(
                    account,
                    entities.notes
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(entities.files).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }

        return willReturnNote
    }

    suspend fun addTootStatusDtoListIntoDataSource(
        account: Account,
        statuses: List<TootStatusDTO>,
        skipExists: Boolean = false
    ): List<Note.Id> {
        val entities = statuses.map {
            it.toEntities(
                account,
            )
        }
        val notes = entities.flatMap {
            it.toots
        } + entities.map {
            it.toot
        }
        val users = entities.flatMap {
            it.users
        }
        val files = entities.flatMap {
            it.files
        }

        if (skipExists) {
            userDataSource.addAll(
                users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            ).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                tootDTOEntityConverter.convertAll(
                    account,
                    notes.filterNot {
                        noteDataSource.exists(Note.Id(account.accountId, it.id))
                    }
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(
                files.filterNot {
                    filePropertyDataSource.find(it.id).isSuccess
                }
            ).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }

        } else {
            userDataSource.addAll(users).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                tootDTOEntityConverter.convertAll(
                    account,
                    notes
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(files).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }
        return entities.map {
            Note.Id(
                account.accountId,
                it.toot.id
            )
        }
    }

    suspend fun addTootStatusDtoIntoDataSource(
        account: Account,
        status: TootStatusDTO,
        skipExists: Boolean = false
    ): Note {
        val entities = status.toEntities(account)
        val willReturnNote = tootDTOEntityConverter.convert(entities.toot, account)
        noteDataSource.add(willReturnNote)
        if (skipExists) {
            userDataSource.addAll(
                entities.users.filterNot {
                    userDataSource.get(it.id).isSuccess
                }
            ).onFailure {
                logger.error("UserDataSourceへの追加に失敗", it)
            }
            noteDataSource.addAll(
                tootDTOEntityConverter.convertAll(
                    account,
                    entities.toots.filterNot {
                        noteDataSource.exists(Note.Id(account.accountId, it.id))
                    }
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            if (!noteDataSource.exists(Note.Id(account.accountId, entities.toot.id))) {
                noteDataSource.add(willReturnNote).onFailure {
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
            noteDataSource.addAll(
                tootDTOEntityConverter.convertAll(
                    account,
                    entities.toots,
                )
            ).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            noteDataSource.add(willReturnNote).onFailure {
                logger.error("NoteDataSourceへの追加に失敗", it)
            }
            filePropertyDataSource.addAll(entities.files).onFailure {
                logger.error("FilePropertyDataSourceへの追加に失敗", it)
            }
        }

        return willReturnNote
    }
}

suspend fun NoteDTO.toEntities(
    account: Account,
    userDTOEntityConverter: UserDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
    instanceType: InstanceInfoType?,
): NoteDTOUnpacked {
    val dtoList = mutableListOf<NoteDTO>()
    dtoList.add(this)


    if (this.reply != null) {

        dtoList.add(this.reply!!)
    }
    if (this.reNote != null) {
        dtoList.add(reNote!!)
    }

    val users = mutableListOf<User>()
    val notes = mutableListOf<NoteDTO>()
    val files = mutableListOf<FileProperty>()

    pickEntities(
        account,
        notes,
        users,
        files,
        userDTOEntityConverter,
        filePropertyDTOEntityConverter,
        instanceType,
    )
    return NoteDTOUnpacked(
        note = this,
        notes = notes,
        users = users,
        files = files
    )
}

private suspend fun NoteDTO.pickEntities(
    account: Account,
    notes: MutableList<NoteDTO>,
    users: MutableList<User>,
    files: MutableList<FileProperty>,
    userDTOEntityConverter: UserDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
    instanceType: InstanceInfoType?,
) {
    val (note, user) = this.toNoteAndUser(
        account,
        userDTOEntityConverter,
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
            filePropertyDTOEntityConverter,
            instanceType,
        )
    }
}

suspend fun NoteDTO.toNoteAndUser(
    account: Account,
    userDTOEntityConverter: UserDTOEntityConverter,
): Pair<NoteDTO, User> {
    val user = userDTOEntityConverter.convert(account, user, false)
    return this to user
}

