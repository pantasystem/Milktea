package net.pantasystem.milktea.data.model.gallery

interface GalleryRepository {

    suspend fun create(createGalleryPost: CreateGalleryPost) : GalleryPost

    suspend fun find(id: GalleryPost.Id) : GalleryPost

    suspend fun like(id: GalleryPost.Id)

    suspend fun unlike(id: GalleryPost.Id)

    suspend fun delete(id: GalleryPost.Id)

    suspend fun update(updateGalleryPost: UpdateGalleryPost) : GalleryPost

}