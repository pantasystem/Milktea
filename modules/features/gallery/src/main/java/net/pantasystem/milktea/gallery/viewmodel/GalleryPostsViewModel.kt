@file:Suppress("UNCHECKED_CAST")

package net.pantasystem.milktea.gallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.app_store.gallery.GalleryPostSendFavoriteStore
import net.pantasystem.milktea.app_store.gallery.GalleryPostsStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.gallery.GalleryPostRelation
import net.pantasystem.milktea.model.gallery.GalleryRepository
import net.pantasystem.milktea.model.user.UserRepository

class GalleryPostsViewModel @AssistedInject constructor(

    private val galleryDataSource: GalleryDataSource,
    galleryRepository: GalleryRepository,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val galleryPostsStoreFactory: GalleryPostsStore.Factory,
    private val loggerFactory: Logger.Factory,
    @Assisted val pageable: Pageable.Gallery,
    @Assisted private var accountId: Long?,
) : ViewModel(), GalleryToggleLikeOrUnlike {


    @AssistedFactory
    interface ViewModelAssistedFactory {

        fun create(pageable: Pageable.Gallery, accountId: Long?): GalleryPostsViewModel
    }
    companion object;

    private val logger by lazy {
        loggerFactory.create("GalleryPostsViewModel")
    }

    private val galleryPostsStore: GalleryPostsStore by lazy {
        galleryPostsStoreFactory.create(pageable, this::getAccount)
    }

    private val _galleryPosts =
        MutableStateFlow<PageableState<List<GalleryPostUiState>>>(
            PageableState.Fixed(
                StateContent.NotExist()))
    val galleryPosts: StateFlow<PageableState<List<GalleryPostUiState>>> = _galleryPosts
    private val lock = Mutex()

    private val galleryPostSendFavoriteStore =
        GalleryPostSendFavoriteStore(galleryRepository)

    private val _error = MutableSharedFlow<Throwable>(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val error: Flow<Throwable> = _error

    private val currentIndexes = MutableStateFlow<Map<GalleryPost.Id, Int>>(emptyMap())

    private val _visibleFileIds = MutableStateFlow<Set<FileProperty.Id>>(emptySet())
    val visibleFileIds: StateFlow<Set<FileProperty.Id>>
        get() = _visibleFileIds

    init {
        galleryDataSource.events().mapNotNull {
            it as? GalleryDataSource.Event.Deleted
        }.onEach { ev ->
            lock.withLock {
                val state = _galleryPosts.value
                val content = state.content
                if (content is StateContent.Exist) {
                    _galleryPosts.value = PageableState.Fixed(
                        content.copy(
                            content.rawContent.filterNot {
                                it.galleryPost.id == ev.galleryPostId
                            }
                        )
                    )
                }
            }
        }.launchIn(viewModelScope + Dispatchers.IO)
        loadInit()
    }

    init {


        val relations = combine(galleryPostsStore.state, galleryDataSource.state) { it, _ ->

            it.convert {
                runBlocking {
                    it.map { id ->
                        async {
                            galleryDataSource.find(id).getOrNull()
                        }

                    }.awaitAll().filterNotNull().map { post ->
                        post to filePropertyDataSource.findIn(post.fileIds).getOrElse { emptyList() }
                    }.filter { postWithFiles ->
                        postWithFiles.second.isNotEmpty()
                    }.map { postWithFiles ->
                        GalleryPostRelation(
                            postWithFiles.first,
                            filePropertyDataSource.findIn(postWithFiles.first.fileIds).getOrElse { emptyList() },
                            userRepository.find(postWithFiles.first.userId, false)
                        )
                    }
                }
            }
        }

        combine(
            relations,
            currentIndexes,
            galleryPostSendFavoriteStore.state,
        ) { posts, indexes, sends ->
            posts.convert {
                it.map { relation ->
                    GalleryPostUiState(
                        galleryPost = relation.galleryPost,
                        files = relation.files,
                        user = relation.user,
                        currentIndex = indexes[relation.galleryPost.id] ?: 0,
                        isFavoriteSending = sends.contains(relation.galleryPost.id),
                    )
                }
            }
        }.onEach {
            _galleryPosts.value = it
            if (it is PageableState.Error) {
                _error.emit(it.throwable)
            }
        }.launchIn(viewModelScope + Dispatchers.IO)


    }

    fun loadInit() {
        if (galleryPostsStore.mutex.isLocked) {
            return
        }
        viewModelScope.launch {
            galleryPostsStore.clear()
            galleryPostsStore.loadPrevious().onFailure {
                logger.error("failed loadInit", it)
            }
        }
    }

//    fun loadFuture() {
//        if (galleryPostsStore.mutex.isLocked) {
//            return
//        }
//        viewModelScope.launch(Dispatchers.IO) {
//            galleryPostsStore.loadFuture()
//        }
//    }

    fun loadPrevious() {
        if (galleryPostsStore.mutex.isLocked) {
            return
        }
        viewModelScope.launch {
            galleryPostsStore.loadPrevious().onFailure {
                logger.error("failed loadPrevious", it)
            }
        }
    }

    fun toggleFavorite(galleryId: GalleryPost.Id) {
        viewModelScope.launch {
            toggle(galleryId)
        }
    }

    fun toggleFileVisibleState(fileId: FileProperty.Id) {
        _visibleFileIds.update {
            it.toMutableSet().also { set ->
                if (set.contains(fileId)) {
                    set.remove(fileId)
                } else {
                    set.add(fileId)
                }
            }
        }
    }

    override suspend fun toggle(galleryId: GalleryPost.Id) {
        runCancellableCatching {
            galleryPostSendFavoriteStore.toggleFavorite(galleryId)
        }.onFailure {
            _error.emit(it)
        }
    }

    suspend fun getAccount(): Account {
        return accountId?.let {
            accountRepository.get(it).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow().also {
            accountId = it.accountId
        }
    }


}

fun GalleryPostsViewModel.Companion.provideFactory(
    factory: GalleryPostsViewModel.ViewModelAssistedFactory,
    pageable: Pageable.Gallery,
    accountId: Long?
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(pageable, accountId) as T
    }
}