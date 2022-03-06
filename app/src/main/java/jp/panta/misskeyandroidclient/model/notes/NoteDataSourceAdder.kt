package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.api.misskey.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.misskey.notes.toEntities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.viewmodel.MiCore

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

fun MiCore.getNoteDataSourceAdder() : NoteDataSourceAdder{
    return NoteDataSourceAdder(getUserDataSource(), getNoteDataSource(), getFilePropertyDataSource())
}