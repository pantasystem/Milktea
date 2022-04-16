package net.pantasystem.milktea.data.model.notes

import net.pantasystem.milktea.data.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.api.misskey.notes.toEntities
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.data.model.users.UserDataSource


class NoteDataSourceAdder(
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
}

