package jp.panta.misskeyandroidclient.di.module

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder



fun MiCore.getNoteDataSourceAdder(): NoteDataSourceAdder {
    return NoteDataSourceAdder(
        getUserDataSource(),
        getNoteDataSource(),
        getFilePropertyDataSource()
    )
}
