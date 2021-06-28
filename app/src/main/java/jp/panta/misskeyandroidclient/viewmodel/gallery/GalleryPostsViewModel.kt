package jp.panta.misskeyandroidclient.viewmodel.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.v12_75_0.GetPosts
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.IllegalVersionException
import jp.panta.misskeyandroidclient.model.UnauthorizedException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.account.page.SincePaginate
import jp.panta.misskeyandroidclient.model.account.page.UntilPaginate
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.gallery.*
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import jp.panta.misskeyandroidclient.api.v12_75_0.GalleryPost as GalleryPostDTO

class GalleryPostsViewModel(
    val pageable: Pageable.Gallery,
    private var accountId: Long?,
    galleryDataSource: GalleryDataSource,
    private val galleryRepository: GalleryRepository,
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    miCore: MiCore
) : ViewModel(), GalleryToggleLikeOrUnlike{

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val pageable: Pageable.Gallery,
        val accountId: Long?,
        val miCore: MiCore
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GalleryPostsViewModel(
                pageable,
                accountId,
                miCore.getGalleryDataSource(),
                miCore.getGalleryRepository(),
                miCore.getAccountRepository(),
                miCore.getMisskeyAPIProvider(),
                miCore
            ) as T
        }
    }

    private val galleryPostsStore = miCore.createGalleryPostsStore(pageable, this::getAccount)

    private val _galleryPosts = MutableStateFlow<State<List<GalleryPostState>>>(State.Fixed(StateContent.NotExist()))
    val galleryPosts: StateFlow<State<List<GalleryPostState>>> = _galleryPosts
    val lock = Mutex()

    private val _error = MutableSharedFlow<Throwable>(extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error: Flow<Throwable> = _error


    init {
        galleryDataSource.events().mapNotNull {
            it as? GalleryDataSource.Event.Deleted
        }.onEach { ev ->
            lock.withLock {
                val state = _galleryPosts.value
                val content = state.content
                if(content is StateContent.Exist) {
                    _galleryPosts.value = State.Fixed(
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
        // FIXME: 毎回Stateオブジェクトを生成してしまうのでユーザーの捜査情報が初期化されてしまうので何とかする
        galleryPostsStore.state.map {
            val content = if(it.content is StateContent.Exist<List<GalleryPost.Id>>) {
                val list = coroutineScope {
                    it.content.rawContent.map { id ->
                        async {
                            runCatching {
                                miCore.getGalleryDataSource().find(id)
                            }.getOrNull()
                        }

                    }.awaitAll().filterNotNull().map { post ->
                        post to miCore.getFilePropertyDataSource().findIn(post.fileIds)
                    }.filter { postWithFiles ->
                        postWithFiles.second.isNotEmpty()
                    }.map { postWithFiles ->
                        GalleryPostState(
                            postWithFiles.first,
                            miCore.getFilePropertyDataSource().findIn(postWithFiles.first.fileIds),
                            miCore.getUserRepository().find(postWithFiles.first.userId, false),
                            this@GalleryPostsViewModel,
                            miCore.getGalleryDataSource(),
                            viewModelScope
                        )
                    }
                }
                StateContent.Exist(list)

            }else{
                StateContent.NotExist()
            }
            when(it) {
                is State.Fixed -> State.Fixed(content)
                is State.Error -> State.Error(content, it.throwable)
                is State.Loading -> State.Loading(content)
            }
        }.onEach {
            this._galleryPosts.value = it
            if(it is State.Error) {
                _error.emit(it.throwable)
            }
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun loadInit() {
       if(galleryPostsStore.mutex.isLocked) {
           return
       }
        viewModelScope.launch(Dispatchers.IO) {
            galleryPostsStore.clear()
            galleryPostsStore.loadPrevious()
        }
    }

    fun loadFuture() {
        if(galleryPostsStore.mutex.isLocked) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            galleryPostsStore.loadFuture()
        }
    }

    fun loadPrevious() {
        if(galleryPostsStore.mutex.isLocked) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            galleryPostsStore.loadPrevious()
        }
    }





    override suspend fun toggle(galleryId: GalleryPost.Id) {
        runCatching {
            val gallery = galleryRepository.find(galleryId) as? GalleryPost.Authenticated
                ?: throw UnauthorizedException()
            if(gallery.isLiked) {
                galleryRepository.unlike(galleryId)
            }else{
                galleryRepository.like(galleryId)
            }
        }.onFailure {
            _error.emit(it)
        }
    }

    suspend fun getAccount(): Account {
        return accountId?.let {
            accountRepository.get(it)
        }?: accountRepository.getCurrentAccount().also {
            accountId = it.accountId
        }
    }

    private fun Account.getMisskeyAPI(): MisskeyAPIV1275 {
        return misskeyAPIProvider.get(this.instanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
    }



}

