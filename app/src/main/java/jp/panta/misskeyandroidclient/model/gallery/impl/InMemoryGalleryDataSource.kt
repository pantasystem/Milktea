package jp.panta.misskeyandroidclient.model.gallery.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryNotFoundException
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class InMemoryGalleryDataSource @Inject constructor(): GalleryDataSource{

    private val galleryEvents = MutableSharedFlow<GalleryDataSource.Event>(extraBufferCapacity = 1000)
    override fun events(): Flow<GalleryDataSource.Event> {
        return galleryEvents
    }

    private var galleries = mapOf<GalleryPost.Id, GalleryPost>()
        set(value) {
            field = value
            _state.value = value
        }
        get() {
            return state.value
        }
    private val lock = Mutex()

    private val _state = MutableStateFlow<Map<GalleryPost.Id, GalleryPost>>(emptyMap())

    override val state: StateFlow<Map<GalleryPost.Id, GalleryPost>> = _state

    override suspend fun add(galleryPost: GalleryPost): AddResult {
        val result = lock.withLock {
            val map = galleries.toMutableMap()
            if(galleries[galleryPost.id] == null){
                map[galleryPost.id] = galleryPost
                galleries = map
                AddResult.CREATED
            }else{
                map[galleryPost.id] = galleryPost
                galleries = map
                AddResult.UPDATED
            }
        }
        if(result == AddResult.CREATED) {
            galleryEvents.tryEmit(GalleryDataSource.Event.Created(galleryPost.id, galleryPost))
        }else if(result == AddResult.UPDATED) {
            galleryEvents.tryEmit(GalleryDataSource.Event.Updated(galleryPost.id, galleryPost))
        }
        return result
    }

    override suspend fun addAll(posts: List<GalleryPost>): List<AddResult> {
        return posts.map {
            add(it)
        }
    }



    override suspend fun find(galleryPostId: GalleryPost.Id): GalleryPost {
        return galleries[galleryPostId] ?: throw GalleryNotFoundException(galleryPostId)
    }

    override suspend fun remove(galleryPostId: GalleryPost.Id): Boolean {
        val result = lock.withLock {
            val map = galleries.toMutableMap()
            map.remove(galleryPostId) != null
        }
        galleryEvents.tryEmit(GalleryDataSource.Event.Deleted(galleryPostId))
        return result
    }

    override suspend fun findAll(): List<GalleryPost> {
        return galleries.values.toList()
    }

    override suspend fun filterByAccountId(accountId: Long): List<GalleryPost> {
        return findAll().filter {
            it.id.accountId == accountId
        }
    }
}

