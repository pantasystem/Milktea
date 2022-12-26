package net.pantasystem.milktea.data.infrastructure.gallery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryNotFoundException
import net.pantasystem.milktea.model.gallery.GalleryPost
import javax.inject.Inject

class InMemoryGalleryDataSource @Inject constructor(): GalleryDataSource {

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

    override suspend fun add(galleryPost: GalleryPost): Result<AddResult> = runCancellableCatching {
        val result = lock.withLock {
            val map = galleries.toMutableMap()
            if(galleries[galleryPost.id] == null){
                map[galleryPost.id] = galleryPost
                galleries = map
                AddResult.Created
            }else{
                map[galleryPost.id] = galleryPost
                galleries = map
                AddResult.Updated
            }
        }
        if(result == AddResult.Created) {
            galleryEvents.tryEmit(GalleryDataSource.Event.Created(galleryPost.id, galleryPost))
        }else if(result == AddResult.Updated) {
            galleryEvents.tryEmit(GalleryDataSource.Event.Updated(galleryPost.id, galleryPost))
        }
        result
    }

    override suspend fun addAll(posts: List<GalleryPost>): Result<List<AddResult>> {
        return Result.success(
            posts.map {
                add(it).getOrElse {
                    AddResult.Canceled
                }
            }
        )
    }



    override suspend fun find(galleryPostId: GalleryPost.Id): Result<GalleryPost> = runCancellableCatching {
        galleries[galleryPostId] ?: throw GalleryNotFoundException(galleryPostId)
    }

    override suspend fun remove(galleryPostId: GalleryPost.Id): Result<Boolean> = runCancellableCatching {
        val result = lock.withLock {
            val map = galleries.toMutableMap()
            map.remove(galleryPostId) != null
        }
        galleryEvents.tryEmit(GalleryDataSource.Event.Deleted(galleryPostId))
        result
    }

    override suspend fun findAll(): Result<List<GalleryPost>> = runCancellableCatching {
        galleries.values.toList()
    }

    override suspend fun filterByAccountId(accountId: Long): Result<List<GalleryPost>> = runCancellableCatching {
        findAll().getOrThrow().filter {
            it.id.accountId == accountId
        }
    }
}

