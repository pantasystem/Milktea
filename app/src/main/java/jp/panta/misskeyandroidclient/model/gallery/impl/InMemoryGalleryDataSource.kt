package jp.panta.misskeyandroidclient.model.gallery.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryNotFoundException
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryGalleryDataSource(
    buffer: Int = 1000
) : GalleryDataSource{

    private val galleryEvents = MutableSharedFlow<GalleryDataSource.Event>(extraBufferCapacity = buffer)
    override fun events(): Flow<GalleryDataSource.Event> {
        return galleryEvents
    }

    private var galleries = mapOf<GalleryPost.Id, GalleryPost>()
    private val lock = Mutex()

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
}