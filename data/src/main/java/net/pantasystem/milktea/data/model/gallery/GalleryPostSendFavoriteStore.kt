package net.pantasystem.milktea.data.model.gallery

import net.pantasystem.milktea.data.model.account.UnauthorizedException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GalleryPostSendFavoriteStore(
    private val galleryRepository: GalleryRepository
) {
    
    private val lock = Mutex()
    private val _state = MutableStateFlow<Set<GalleryPost.Id>>(emptySet())
    val state: StateFlow<Set<GalleryPost.Id>> = _state
    
    suspend fun toggleFavorite(galleryId: GalleryPost.Id) {
        try{
            lock.withLock {
                _state.value = _state.value.toMutableSet().also {
                    it.add(galleryId)
                }
            }
            val gallery = galleryRepository.find(galleryId) as? GalleryPost.Authenticated
                ?: throw UnauthorizedException()
            if(gallery.isLiked) {
                galleryRepository.unlike(galleryId)
            }else{
                galleryRepository.like(galleryId)
            }
        }catch(e: Throwable) {
            throw e
        }finally{
            lock.withLock {
                _state.value = _state.value.toMutableSet().also {
                    it.remove(galleryId)
                }
            }
        }

    }
    
}