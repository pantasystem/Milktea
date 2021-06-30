package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.FuturePaginatorController
import jp.panta.misskeyandroidclient.model.PreviousPagingController
import jp.panta.misskeyandroidclient.model.StateLocker
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


interface GalleryPostsStore : StateLocker{
    val state: Flow<PageableState<List<GalleryPost.Id>>>

    suspend fun loadPrevious()
    suspend fun loadFuture()
    suspend fun clear()
}

fun MiCore.createGalleryPostsStore(
    pageable: Pageable.Gallery,
    getAccount: suspend () -> Account,
) : GalleryPostsStore{
    return if(pageable is Pageable.Gallery.ILikedPosts) {
        LikedGalleryPostStoreImpl(
            getAccount,
            this.getMisskeyAPIProvider(),
            this.getFilePropertyDataSource(),
            this.getUserDataSource(),
            this.getGalleryDataSource(),
            this.getEncryption()
        )
    }else{
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

class GalleryPostsStoreImpl(
    pageable: Pageable.Gallery,
    getAccount: suspend ()-> Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    userDataSource: UserDataSource,
    galleryDataSource: GalleryDataSource,
    encryption: Encryption
) : GalleryPostsStore{

    override val mutex: Mutex = Mutex()

    private val galleryPostState = GalleryPostsState()
    private val entityAdder = GalleryPostsAdder(getAccount, filePropertyDataSource, userDataSource, galleryDataSource)
    private val loader = GalleryPostsLoader(pageable, galleryPostState, misskeyAPIProvider, getAccount, encryption)
    private val previousPagingController = PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController = FuturePaginatorController(entityAdder, this, galleryPostState, loader)
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
    getAccount: suspend ()-> Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    filePropertyDataSource: FilePropertyDataSource,
    userDataSource: UserDataSource,
    galleryDataSource: GalleryDataSource,
    encryption: Encryption
) : GalleryPostsStore{

    override val mutex: Mutex = Mutex()

    private val galleryPostState = LikedGalleryPostsState()
    private val entityAdder = LikedGalleryPostsAdder(getAccount, filePropertyDataSource, userDataSource, galleryDataSource)
    private val loader = LikedGalleryPostsLoader(galleryPostState, misskeyAPIProvider, getAccount, encryption)
    private val previousPagingController = PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController = FuturePaginatorController(entityAdder, this, galleryPostState, loader)
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