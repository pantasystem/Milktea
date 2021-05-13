package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.model.AddResult
import kotlinx.coroutines.flow.Flow

interface GalleryDataSource {

    sealed class Event {
        abstract val galleryPostId: GalleryPost.Id
        data class Created(override val galleryPostId: GalleryPost.Id, val galleryPost: GalleryPost) : Event()
        data class Updated(override val galleryPostId: GalleryPost.Id, val galleryPost: GalleryPost) : Event()
        data class Deleted(override val galleryPostId: GalleryPost.Id) : Event()
    }

    fun events(): Flow<Event>

    suspend fun add(galleryPost: GalleryPost) : AddResult
    suspend fun remove(galleryPostId: GalleryPost.Id) : Boolean
    suspend fun find(galleryPostId: GalleryPost.Id) : GalleryPost
    suspend fun addAll(posts: List<GalleryPost>) : List<AddResult>
}

