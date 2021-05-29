package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.api.v12_75_0.GetPosts
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import retrofit2.Response
import java.lang.IllegalStateException
import jp.panta.misskeyandroidclient.api.v12_75_0.GalleryPost as GalleryPostDTO

/**
 * EntityはDataSourceが保持しているので、ここではタイムラインの順番を保持する
 */
class GalleryPostsState : PageableState<GalleryPost.Id>, IdGetter<String> {

    private val _state = MutableStateFlow<State<List<GalleryPost.Id>>>(State.Fixed(StateContent.NotExist()))
    override val state: Flow<State<List<GalleryPost.Id>>> = _state

    private fun getList(): List<GalleryPost.Id> {
        return (_state.value.content as? StateContent.Exist)?.rawContent?: emptyList()
    }
    override suspend fun getSinceId(): String? {
        return getList().firstOrNull()?.galleryId
    }

    override suspend fun getUntilId(): String? {
        return getList().lastOrNull()?.galleryId
    }

    override fun getState(): State<List<GalleryPost.Id>> {
        return _state.value
    }

    override fun setState(state: State<List<GalleryPost.Id>>) {
        _state.value = state
    }
}

class GalleryPostsAdder(
    private val account: Account,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: UserDataSource
) : EntityAdder<GalleryPostDTO, GalleryPost.Id> {

    override suspend fun addAll(list: List<GalleryPostDTO>): List<GalleryPost.Id> {
        return list.map {
            it.toEntity(account, filePropertyDataSource, userDataSource).id
        }
    }
}

class GalleryPostLoader (
    private val pageable: Pageable.Gallery,
    private val idGetter: IdGetter<String>,
    private val api: MisskeyAPIV1275,
    private val account: Account,
    private val encryption: Encryption
) : FutureLoader<GalleryPostDTO>, PreviousLoader<GalleryPostDTO>{
    init{
        if(pageable is Pageable.Gallery.ILikedPosts){
            throw IllegalArgumentException("${pageable::class.simpleName}は対応していません。")
        }
    }

    override suspend fun loadFuture(): Response<List<GalleryPostDTO>> {
        return api(untilId = idGetter.getSinceId()).invoke()
    }

    override suspend fun loadPrevious(): Response<List<GalleryPostDTO>> {
        return api(untilId = idGetter.getUntilId()).invoke()
    }


    fun api(sinceId: String? = null, untilId: String? = null) : suspend ()-> Response<List<GalleryPostDTO>>{
        val i = account.getI(encryption)
        when(pageable) {
            is Pageable.Gallery.MyPosts -> {
                return {
                    api.myGalleryPosts(GetPosts(i, sinceId = sinceId, untilId = untilId, limit = 20,))
                }
            }
            is Pageable.Gallery.ILikedPosts -> {
                throw IllegalStateException()
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