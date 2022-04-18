package net.pantasystem.milktea.data.model.notes

import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.model.toEntities
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource


class NoteDataSourceAdder(
    private val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    private val noteDataSource: NoteDataSource,
    private val filePropertyDataSource: net.pantasystem.milktea.model.drive.FilePropertyDataSource
) {


    suspend fun addNoteDtoToDataSource(account: net.pantasystem.milktea.model.account.Account, noteDTO: NoteDTO): Note {
        val entities = noteDTO.toEntities(account)
        userDataSource.addAll(entities.users)
        noteDataSource.addAll(entities.notes)
        filePropertyDataSource.addAll(entities.files)
        return entities.note
    }
}

