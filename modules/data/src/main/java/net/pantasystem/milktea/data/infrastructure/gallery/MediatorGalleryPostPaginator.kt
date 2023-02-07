package net.pantasystem.milktea.data.infrastructure.gallery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.app_store.gallery.GalleryPostsStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.FuturePagingController
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.GalleryPostDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject


class GalleryPostsStoreImpl(
    pageable: Pageable.Gallery,
    getAccount: suspend () -> Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    galleryPostDTOEntityConverter: GalleryPostDTOEntityConverter,
) : GalleryPostsStore {

    class Factory @Inject constructor(
        val misskeyAPIProvider: MisskeyAPIProvider,
        val filePropertyDataSource: FilePropertyDataSource,
        val userDataSource: UserDataSource,
        val galleryDataSource: GalleryDataSource,
        val userDTOEntityConverter: UserDTOEntityConverter,
        val galleryPostDTOEntityConverter: GalleryPostDTOEntityConverter,
    ) : GalleryPostsStore.Factory {
        override fun create(
            pageable: Pageable.Gallery,
            getAccount: suspend () -> Account,
        ): GalleryPostsStore {
            return if (pageable is Pageable.Gallery.ILikedPosts) {
                LikedGalleryPostStoreImpl(
                    getAccount,
                    misskeyAPIProvider,
                    galleryDataSource,
                    galleryPostDTOEntityConverter
                )
            } else {
                GalleryPostsStoreImpl(
                    pageable,
                    getAccount,
                    misskeyAPIProvider,
                    galleryPostDTOEntityConverter,
                )
            }
        }
    }


    override val mutex: Mutex = Mutex()

    private val galleryPostState = GalleryPostsState()
    private val entityAdder =
        GalleryPostsConverter(
            getAccount,
            galleryPostDTOEntityConverter,
        )
    private val loader = GalleryPostsLoader(pageable, galleryPostState, misskeyAPIProvider, getAccount)
    private val previousPagingController =
        PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController =
        FuturePagingController(entityAdder, this, galleryPostState, loader)

    override suspend fun loadPrevious(): Result<Int> {
        return previousPagingController.loadPrevious()
    }

    override suspend fun loadFuture(): Result<Int> {
        return futurePaginatorController.loadFuture()
    }

    override suspend fun clear() {
        mutex.withLock {
            galleryPostState.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override val state: Flow<PageableState<List<GalleryPost.Id>>> = galleryPostState.getFlow()
}

class LikedGalleryPostStoreImpl(
    getAccount: suspend () -> Account,
    misskeyAPIProvider: MisskeyAPIProvider,
    galleryDataSource: GalleryDataSource,
    galleryPostDTOEntityConverter: GalleryPostDTOEntityConverter,
) : GalleryPostsStore {

    override val mutex: Mutex = Mutex()

    private val galleryPostState = LikedGalleryPostsState()
    private val entityAdder = LikedGalleryPostsConverter(
        getAccount,
        galleryDataSource,
        galleryPostDTOEntityConverter = galleryPostDTOEntityConverter,
    )
    private val loader =
        LikedGalleryPostsLoader(galleryPostState, misskeyAPIProvider, getAccount)
    private val previousPagingController =
        PreviousPagingController(entityAdder, this, galleryPostState, loader)
    private val futurePaginatorController =
        FuturePagingController(entityAdder, this, galleryPostState, loader)

    override suspend fun loadPrevious(): Result<Int> {
        return previousPagingController.loadPrevious()
    }

    override suspend fun loadFuture(): Result<Int> {
        return futurePaginatorController.loadFuture()
    }

    override suspend fun clear() {
        mutex.withLock {
            galleryPostState.setState(PageableState.Fixed(StateContent.NotExist()))
        }
    }

    override val state: Flow<PageableState<List<GalleryPost.Id>>> = galleryPostState.getFlow()
}