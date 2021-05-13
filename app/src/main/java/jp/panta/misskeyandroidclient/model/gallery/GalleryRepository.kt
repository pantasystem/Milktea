package jp.panta.misskeyandroidclient.model.gallery

interface GalleryRepository {

    suspend fun create() : GalleryPost

    suspend fun find(id: GalleryPost.Id) : GalleryPost

    suspend fun like(id: GalleryPost)

    suspend fun unlike(id: GalleryPost)

    suspend fun delete(id: GalleryPost)

    suspend fun update(id: GalleryPost) : GalleryPost

}