package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.v12_75_0.GetPosts
import jp.panta.misskeyandroidclient.api.v12_75_0.LikedGalleryPost
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Response

data class LikedGalleryPostId(
    val id: String,
    val postId: GalleryPost.Id
)

class LikedGalleryPostsState : PageableState<LikedGalleryPostId>, IdGetter<String>, GetGalleryPostsStateFlow {

    private val _state = MutableStateFlow<State<List<LikedGalleryPostId>>>(State.Fixed(StateContent.NotExist()))

    override fun getFlow(): Flow<State<List<GalleryPost.Id>>> {
        return _state.map {
            val content = if(it.content is StateContent.Exist) {
                StateContent.Exist(
                    it.content.rawContent.map { liked ->
                        liked.postId
                    }
                )
            }else{
                StateContent.NotExist()
            }
            when(it) {
                is State.Fixed -> {
                    State.Fixed(content)
                }
                is State.Error -> {
                    State.Error(content, it.throwable)
                }
                is State.Loading -> {
                    State.Loading(content)
                }
            }

        }
    }


    private fun getList(): List<LikedGalleryPostId> {
        return (_state.value.content as? StateContent.Exist)?.rawContent?: emptyList()
    }
    override suspend fun getSinceId(): String? {
        return getList().firstOrNull()?.id
    }

    override suspend fun getUntilId(): String? {
        return getList().lastOrNull()?.id
    }


    override val state: Flow<State<List<LikedGalleryPostId>>> = _state

    override fun getState(): State<List<LikedGalleryPostId>> {
        return _state.value
    }

    override fun setState(state: State<List<LikedGalleryPostId>>) {
        this._state.value = state
    }
}

class LikedGalleryPostsAdder(
    private val getAccount: suspend () -> Account,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: UserDataSource,
    private val galleryDataSource: GalleryDataSource
) : EntityAdder<LikedGalleryPost, LikedGalleryPostId> {

    override suspend fun addAll(list: List<LikedGalleryPost>): List<LikedGalleryPostId> {
        val account = getAccount.invoke()
        return list.map {
            LikedGalleryPostId(
                it.id,
                it.post.toEntity(account, filePropertyDataSource, userDataSource).also { post ->
                    galleryDataSource.add(post)
                }.id
            )
        }
    }
}

class LikedGalleryPostsLoader(
    private val idGetter: IdGetter<String>,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val getAccount: suspend ()->Account,
    private val encryption: Encryption
) : FutureLoader<LikedGalleryPost>, PreviousLoader<LikedGalleryPost> {

    override suspend fun loadFuture(): Response<List<LikedGalleryPost>> {
        val api = misskeyAPIProvider.get(getAccount.invoke().instanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
        return api.likedGalleryPosts(GetPosts(
           sinceId = idGetter.getSinceId(),
            i = getAccount.invoke().getI(encryption)
        ))
    }

    override suspend fun loadPrevious(): Response<List<LikedGalleryPost>> {
        val api = misskeyAPIProvider.get(getAccount.invoke().instanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
        return api.likedGalleryPosts(GetPosts(
            untilId = idGetter.getUntilId(),
            i = getAccount.invoke().getI(encryption)
        ))
    }
}