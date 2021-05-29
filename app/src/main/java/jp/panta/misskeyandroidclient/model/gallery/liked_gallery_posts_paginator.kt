package jp.panta.misskeyandroidclient.model.gallery

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
    private val account: Account,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val userDataSource: UserDataSource
) : EntityAdder<LikedGalleryPost, LikedGalleryPostId> {

    override suspend fun addAll(list: List<LikedGalleryPost>): List<LikedGalleryPostId> {
        return list.map {
            LikedGalleryPostId(
                it.id,
                it.post.toEntity(account, filePropertyDataSource, userDataSource).id
            )
        }
    }
}

class LikedGalleryPostsLoader(
    private val idGetter: IdGetter<String>,
    private val misskeyAPIV1275: MisskeyAPIV1275,
    private val account: Account,
    private val encryption: Encryption
) : FutureLoader<LikedGalleryPost>, PreviousLoader<LikedGalleryPost> {

    override suspend fun loadFuture(): Response<List<LikedGalleryPost>> {
        return misskeyAPIV1275.likedGalleryPosts(GetPosts(
           sinceId = idGetter.getSinceId(),
            i = account.getI(encryption)
        ));
    }

    override suspend fun loadPrevious(): Response<List<LikedGalleryPost>> {
        return misskeyAPIV1275.likedGalleryPosts(GetPosts(
            untilId = idGetter.getUntilId(),
            i = account.getI(encryption)
        ));
    }
}