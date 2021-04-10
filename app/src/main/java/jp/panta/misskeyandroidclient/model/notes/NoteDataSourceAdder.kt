package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class NoteDataSourceAdder(
    private val userDataSource: UserDataSource,
    private val noteDataSource: NoteDataSource
) {


    suspend fun addNoteDtoToDataSource(account: Account, noteDTO: NoteDTO): Note {
        val entities = noteDTO.toEntities(account)
        userDataSource.addAll(entities.third)
        noteDataSource.addAll(entities.second)
        return entities.first
    }
}