package net.pantasystem.milktea.model.gallery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.model.AddResult

interface GalleryDataSource {

    sealed class Event {
        abstract val galleryPostId: GalleryPost.Id
        data class Created(override val galleryPostId: GalleryPost.Id, val galleryPost: GalleryPost) : Event()
        data class Updated(override val galleryPostId: GalleryPost.Id, val galleryPost: GalleryPost) : Event()
        data class Deleted(override val galleryPostId: GalleryPost.Id) : Event()
    }

    fun events(): Flow<Event>
    val state: StateFlow<Map<GalleryPost.Id, GalleryPost>>

    suspend fun add(galleryPost: GalleryPost) : Result<AddResult>
    suspend fun remove(galleryPostId: GalleryPost.Id) : Result<Boolean>
    suspend fun find(galleryPostId: GalleryPost.Id) : Result<GalleryPost>
    suspend fun addAll(posts: List<GalleryPost>) : Result<List<AddResult>>
    suspend fun findAll() : Result<List<GalleryPost>>
    suspend fun filterByAccountId(accountId: Long) : Result<List<GalleryPost>>
}

