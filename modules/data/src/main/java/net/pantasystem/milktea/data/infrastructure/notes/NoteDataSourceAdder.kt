package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.infrastructure.toEntities
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteDataSourceAdder @Inject constructor(
    private val userDataSource: UserDataSource,
    private val noteDataSource: NoteDataSource,
    private val filePropertyDataSource: FilePropertyDataSource
) {


    suspend fun addNoteDtoToDataSource(account: Account, noteDTO: NoteDTO): Note {
        val entities = noteDTO.toEntities(account)
        userDataSource.addAll(entities.users)
        noteDataSource.addAll(entities.notes)
        filePropertyDataSource.addAll(entities.files)
        return entities.note
    }

    suspend fun addTootStatusDtoIntoDataSource(account: Account, status: TootStatusDTO): Note {
        val entities = status.toEntities(account)
        userDataSource.addAll(entities.users)
        noteDataSource.addAll(entities.notes)
        noteDataSource.add(entities.note)
        filePropertyDataSource.addAll(entities.files)
        return entities.note
    }
}

