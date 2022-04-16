package net.pantasystem.milktea.data.model.gallery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.data.model.AddResult

interface GalleryDataSource {

    sealed class Event {
        abstract val galleryPostId: GalleryPost.Id
        data class Created(override val galleryPostId: GalleryPost.Id, val galleryPost: GalleryPost) : Event()
        data class Updated(override val galleryPostId: GalleryPost.Id, val galleryPost: GalleryPost) : Event()
        data class Deleted(override val galleryPostId: GalleryPost.Id) : Event()
    }

    fun events(): Flow<Event>
    val state: StateFlow<Map<GalleryPost.Id, GalleryPost>>

    suspend fun add(galleryPost: GalleryPost) : AddResult
    suspend fun remove(galleryPostId: GalleryPost.Id) : Boolean
    suspend fun find(galleryPostId: GalleryPost.Id) : GalleryPost
    suspend fun addAll(posts: List<GalleryPost>) : List<AddResult>
    suspend fun findAll() : List<GalleryPost>
    suspend fun filterByAccountId(accountId: Long) : List<GalleryPost>
}

