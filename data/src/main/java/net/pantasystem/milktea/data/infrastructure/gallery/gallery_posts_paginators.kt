package net.pantasystem.milktea.data.infrastructure.gallery

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.api.misskey.v12_75_0.GetPosts
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275
import net.pantasystem.milktea.data.infrastructure.*
import net.pantasystem.milktea.model.instance.IllegalVersionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.UserDataSource
import retrofit2.Response
import java.lang.IllegalStateException
import net.pantasystem.milktea.api.misskey.v12_75_0.GalleryPost as GalleryPostDTO


interface GetGalleryPostsStateFlow {
    fun getFlow(): Flow<PageableState<List<GalleryPost.Id>>>
}
/**
 * EntityはDataSourceが保持しているので、ここではタイムラインの順番を保持する
 */
class GalleryPostsState : PaginationState<GalleryPost.Id>, IdGetter<String>, GetGalleryPostsStateFlow {

    private val _state = MutableStateFlow<PageableState<List<GalleryPost.Id>>>(PageableState.Fixed(
        StateContent.NotExist()))
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

class GalleryPostsConverter(
    private val getAccount: suspend ()-> Account,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: UserDataSource,
    private val galleryDataSource: GalleryDataSource
) : EntityConverter<GalleryPostDTO, GalleryPost.Id> {

    override suspend fun convertAll(list: List<GalleryPostDTO>): List<GalleryPost.Id> {
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
) : FutureLoader<GalleryPostDTO>, PreviousLoader<GalleryPostDTO> {
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
                    api.myGalleryPosts(
                        GetPosts(
                        i,
                        sinceId = sinceId,
                        untilId = untilId,
                        limit = 20,
                    )
                    )
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