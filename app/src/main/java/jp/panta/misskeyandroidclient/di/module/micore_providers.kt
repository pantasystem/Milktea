package jp.panta.misskeyandroidclient.di.module

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.data.infrastructure.gallery.GalleryPostsStore
import net.pantasystem.milktea.data.infrastructure.gallery.GalleryPostsStoreImpl
import net.pantasystem.milktea.data.infrastructure.gallery.LikedGalleryPostStoreImpl
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingService
import net.pantasystem.milktea.data.infrastructure.notes.renote.RenotesPagingServiceImpl
import net.pantasystem.milktea.model.notes.Note


fun MiCore.createGalleryPostsStore(
    pageable: Pageable.Gallery,
    getAccount: suspend () -> Account,
): GalleryPostsStore {
    return if (pageable is Pageable.Gallery.ILikedPosts) {
        LikedGalleryPostStoreImpl(
            getAccount,
            this.getMisskeyAPIProvider(),
            this.getFilePropertyDataSource(),
            this.getUserDataSource(),
            this.getGalleryDataSource(),
            this.getEncryption()
        )
    } else {
        GalleryPostsStoreImpl(
            pageable,
            getAccount,
            this.getMisskeyAPIProvider(),
            this.getFilePropertyDataSource(),
            this.getUserDataSource(),
            this.getGalleryDataSource(),
            this.getEncryption()
        )
    }
}


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