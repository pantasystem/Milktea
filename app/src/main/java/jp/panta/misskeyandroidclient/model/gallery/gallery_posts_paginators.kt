package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.v12_75_0.GetPosts
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.IllegalVersionException
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response
import java.lang.IllegalStateException
import jp.panta.misskeyandroidclient.api.v12_75_0.GalleryPost as GalleryPostDTO


interface GetGalleryPostsStateFlow {
    fun getFlow(): Flow<PageableState<List<GalleryPost.Id>>>
}
/**
 * EntityはDataSourceが保持しているので、ここではタイムラインの順番を保持する
 */
class GalleryPostsState : PaginationState<GalleryPost.Id>, IdGetter<String>, GetGalleryPostsStateFlow {

    private val _state = MutableStateFlow<PageableState<List<GalleryPost.Id>>>(PageableState.Fixed(StateContent.NotExist()))
    override val state: Flow<PageableState<List<GalleryPost.Id>>> = _state

    override fun getFlow(): Flow<PageableState<List<GalleryPost.Id>>> {
        return state
    }

    private fun getList(): List<GalleryPost.Id> {
        return (_state.value.content as? StateContent.Exist)?.rawContent?: emptyList()
    }
    override suspend fun getSinceId(): String? {
        return getList().firstOrNull()?.galleryId
    }

    override suspend fun getUntilId(): String? {
        return getList().lastOrNull()?.galleryId
    }

    override fun getState(): PageableState<List<GalleryPost.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<GalleryPost.Id>>) {
        _state.value = state
    }
}

class GalleryPostsAdder(
    private val getAccount: suspend ()-> Account,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: UserDataSource,
    private val galleryDataSource: GalleryDataSource
) : EntityAdder<GalleryPostDTO, GalleryPost.Id> {

    override suspend fun addAll(list: List<GalleryPostDTO>): List<GalleryPost.Id> {
        return list.map {
            it.toEntity(getAccount.invoke(), filePropertyDataSource, userDataSource).also { post ->
                galleryDataSource.add(post)
            }.id
        }
    }
}

class GalleryPostsLoader (
    private val pageable: Pageable.Gallery,
    private val idGetter: IdGetter<String>,
    private val apiProvider: MisskeyAPIProvider,
    private val getAccount: suspend () -> Account,
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


    suspend fun api(sinceId: String? = null, untilId: String? = null) : suspend ()-> Response<List<GalleryPostDTO>>{
        val i = getAccount.invoke().getI(encryption)
        val api = apiProvider.get(getAccount.invoke().instanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
        when(pageable) {
            is Pageable.Gallery.MyPosts -> {
                return {
                    api.myGalleryPosts(GetPosts(
                        i,
                        sinceId = sinceId,
                        untilId = untilId,
                        limit = 20,
                    ))
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