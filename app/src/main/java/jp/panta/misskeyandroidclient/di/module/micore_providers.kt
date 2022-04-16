package jp.panta.misskeyandroidclient.di.module

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.account.page.Pageable
import net.pantasystem.milktea.data.model.drive.FilePropertyPagingStore
import net.pantasystem.milktea.data.model.gallery.GalleryPostsStore
import net.pantasystem.milktea.data.model.gallery.GalleryPostsStoreImpl
import net.pantasystem.milktea.data.model.gallery.GalleryRepository
import net.pantasystem.milktea.data.model.gallery.LikedGalleryPostStoreImpl
import net.pantasystem.milktea.data.model.gallery.impl.GalleryRepositoryImpl
import net.pantasystem.milktea.data.model.notes.Note
import net.pantasystem.milktea.data.model.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.model.notes.renote.RenotesPagingService
import net.pantasystem.milktea.data.model.notes.renote.RenotesPagingServiceImpl

fun MiCore.filePropertyPagingStore(getAccount: suspend () -> Account, currentDirectoryId: String?) : FilePropertyPagingStore {
    return FilePropertyPagingStore(
        currentDirectoryId,
        getAccount,
        this.getMisskeyAPIProvider(),
        this.getFilePropertyDataSource(),
        this.getEncryption()
    )
}

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

fun MiCore.createGalleryRepository() : GalleryRepository {
    return GalleryRepositoryImpl(
        getMisskeyAPIProvider(),
        getGalleryDataSource(),
        getEncryption(),
        getFileUploaderProvider(),
        getUserDataSource(),
        getFilePropertyDataSource(),
        getAccountRepository(),
    )
}

fun MiCore.getNoteDataSourceAdder() : NoteDataSourceAdder {
    return NoteDataSourceAdder(getUserDataSource(), getNoteDataSource(), getFilePropertyDataSource())
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