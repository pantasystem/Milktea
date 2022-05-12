package jp.panta.misskeyandroidclient.di.module

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingService
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingServiceImpl
import net.pantasystem.milktea.model.notes.Note



fun MiCore.getNoteDataSourceAdder(): NoteDataSourceAdder {
    return NoteDataSourceAdder(
        getUserDataSource(),
        getNoteDataSource(),
        getFilePropertyDataSource()
    )
}

fun MiCore.createRenotesPagingService(targetNoteId: Note.Id): RenotesPagingService {
    return RenotesPagingServiceImpl(
        targetNoteId,
        this.getMisskeyAPIProvider(),
        this.getAccountRepository(),
        NoteDataSourceAdder(
            this.getUserDataSource(),
            this.getNoteDataSource(),
            this.getFilePropertyDataSource()
        ),
        this.getEncryption(),
    )
}