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
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.model.gallery.GalleryRepository
import jp.panta.misskeyandroidclient.model.gallery.toEntity
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import java.util.Collections.addAll
import jp.panta.misskeyandroidclient.api.v12_75_0.GalleryPost as GalleryPostDTO

class GalleryPostsViewModel(
    val pageable: Pageable.Gallery,
    private var accountId: Long?,
    private val galleryDataSource: GalleryDataSource,
    private val galleryRepository: GalleryRepository,
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: UserDataSource,
    private val encryption: Encryption,
    private val logger: Logger?
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
                miCore.getFilePropertyDataSource(),
                miCore.getUserDataSource(),
                miCore.getEncryption(),
                miCore.loggerFactory.create("GalleryPostVM")
            ) as T
        }
    }

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

    fun loadInit() {
        if(lock.isLocked) {
            logger?.debug("locked")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {

            load()
        }
    }

    fun loadFuture() {
        if(lock.isLocked) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val sinceId = (_galleryPosts.value.content as? StateContent.Exist)?.rawContent?.firstOrNull()?.galleryPost?.id
            if(pageable is SincePaginate) {
                load(sinceId = sinceId?.galleryId)
            }
        }
    }

    fun loadPrevious() {
        if(lock.isLocked) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            if(pageable is UntilPaginate) {
                val untilId = (_galleryPosts.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.galleryPost?.id
                load(untilId = untilId?.galleryId)
            }
        }
    }

    private suspend fun load(sinceId: String? = null, untilId: String? = null) {
        logger?.debug("call load sinceId:$sinceId, untilId:$untilId")
        require(pageable is SincePaginate || sinceId != null) {
            "sinceId読み込みには対応していません。"
        }
        require(pageable is UntilPaginate || untilId != null) {
            "untilId読み込みには対応していません。"
        }

        lock.withLock {
            val state = _galleryPosts.value
            val content = state.content
            _galleryPosts.value = if(sinceId == null && untilId == null) State.Loading(StateContent.NotExist()) else State.Loading(state.content)
            runCatching {
                val body = getAccount().getGalleryPosts(sinceId, untilId).invoke().throwIfHasError()
                    .body()
                requireNotNull(body)
                body.map {
                    it.toEntity(getAccount(), filePropertyDataSource, userDataSource)
                }.map {
                    GalleryPostState(
                        it,
                        it.fileIds.map { fileId -> filePropertyDataSource.find(fileId) },
                        userDataSource.get(it.userId),
                        this,
                        galleryDataSource,
                        viewModelScope,
                        Dispatchers.IO
                    )
                }
            }.onSuccess { loaded ->
                if((sinceId == null && untilId == null) || content is StateContent.NotExist) {
                    _galleryPosts.value = State.Fixed(StateContent.Exist(loaded))
                }else if(sinceId != null && content is StateContent.Exist) {
                    val list = content.rawContent.toMutableList()
                    list.addAll(0, loaded.asReversed())
                    _galleryPosts.value = State.Fixed(StateContent.Exist(list))
                }else if(untilId != null && content is StateContent.Exist){
                    val list = content.rawContent.toMutableList().also {
                        it.addAll(loaded)
                    }
                    _galleryPosts.value = State.Fixed(StateContent.Exist(ArrayList(list)))
                }
            }.onFailure {
                logger?.debug("load error:$it")
                _galleryPosts.value = State.Error(state.content, it)
                _error.emit(it)
            }


        }
    }


    /**
     * 使用しているアカウントを変更します。
     * 使用した場合状態が初期化されます。
     */
    suspend fun setAccount(accountId: Long) {
        this.accountId = accountId
        load()
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

    private fun Account.getGalleryPosts(sinceId: String? = null, untilId: String? = null) : suspend ()->Response<List<GalleryPostDTO>>{

        val api = getMisskeyAPI()
        val i = getI(encryption)
        when(pageable) {
            is Pageable.Gallery.MyPosts -> {
                return {
                    api.myGalleryPosts(GetPosts(i, sinceId = sinceId, untilId = untilId, limit = 20,))
                }
            }
            is Pageable.Gallery.ILikedPosts -> {
                return {
                    api.likedGalleryPosts(GetPosts(i, sinceId = sinceId, untilId = untilId, limit = 20,))
                }
            }
            is Pageable.Gallery.User -> {
                return {
                    api.userPosts(GetPosts(i, sinceId = sinceId, untilId = untilId, limit = 20, userId = pageable.userId))
                }
            }
            is Pageable.Gallery.Posts -> {
                return {
                    api.galleryPosts(GetPosts(i, sinceId = sinceId, untilId = untilId, limit = 20))
                }
            }
            is Pageable.Gallery.Featured -> {
                return {
                    api.featuredGalleries(I(i))
                }
            }
            is Pageable.Gallery.Popular -> {
                return {
                    api.popularGalleries(I(i))
                }
            }
        }
    }

}

