package net.pantasystem.milktea.data.model.gallery

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.model.FuturePagingController
import net.pantasystem.milktea.data.model.PreviousPagingController
import net.pantasystem.milktea.data.model.StateLocker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost


interface GalleryPostsStore : StateLocker {
    val state: Flow<PageableState<List<GalleryPost.Id>>>

    suspend fun loadPrevious()
    suspend fun loadFuture()
    suspend fun clear()
}


class GalleryPostsStoreImpl(
    pageable: net.pantasystem.milktea.model.account.page.Pageable.Gallery,
    getAccount: suspend () -> net.pantasystem.milktea.model.account.Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: net.pantasystem.milktea.model.drive.FilePropertyDataSource,
    userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    galleryDataSource: GalleryDataSource,
    encryption: Encryption
) : GalleryPostsStore {

    override val mutex: Mutex = Mutex()

    private val galleryPostState = GalleryPostsState()
    private val entityAdder =
        GalleryPostsConverter(getAccount, filePropertyDataSource, userDataSource, galleryDataSource)
    private val loader =
        GalleryPostsLoader(pageable, galleryPostState, misskeyAPIProvider, getAccount, encryption)
    private val previousPagingController =
        PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController =
        FuturePagingController(entityAdder, this, galleryPostState, loader)

    override suspend fun loadPrevious() {
        return previousPagingController.loadPrevious()
    }

    override suspend fun loadFuture() {
        futurePaginatorController.loadFuture()
    }

    override suspend fun clear() {
        mutex.withLock {
            galleryPostState.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override val state: Flow<PageableState<List<GalleryPost.Id>>> = galleryPostState.getFlow()
}

class LikedGalleryPostStoreImpl(
    getAccount: suspend () -> net.pantasystem.milktea.model.account.Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: net.pantasystem.milktea.model.drive.FilePropertyDataSource,
    userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    galleryDataSource: GalleryDataSource,
    encryption: Encryption
) : GalleryPostsStore {

    override val mutex: Mutex = Mutex()

    private val galleryPostState = LikedGalleryPostsState()
    private val entityAdder = LikedGalleryPostsConverter(
        getAccount,
        filePropertyDataSource,
        userDataSource,
        galleryDataSource
    )
    private val loader =
        LikedGalleryPostsLoader(galleryPostState, misskeyAPIProvider, getAccount, encryption)
    private val previousPagingController =
        PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController =
        FuturePagingController(entityAdder, this, galleryPostState, loader)

    override suspend fun loadPrevious() {
        return previousPagingController.loadPrevious()
    }

    override suspend fun loadFuture() {
        futurePaginatorController.loadFuture()
    }

    override suspend fun clear() {
        mutex.withLock {
            galleryPostState.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override val state: Flow<PageableState<List<GalleryPost.Id>>> = galleryPostState.getFlow()
}