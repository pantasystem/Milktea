package net.pantasystem.milktea.api.misskey.v12_75_0

import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11Diff
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12Diff
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.I
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV1275Diff {
    @POST("/api/gallery/featured")
    suspend fun featuredGalleries(@Body i: I) : Response<List<GalleryPost>>

    @POST("/api/gallery/popular")
    suspend fun popularGalleries(@Body i: I) : Response<List<GalleryPost>>

    @POST("/api/gallery/posts")
    suspend fun galleryPosts(@Body getGalleryPost: GetPosts) : Response<List<GalleryPost>>

    @POST("/api/gallery/posts/create")
    suspend fun createGallery(@Body createGallery: CreateGallery) : Response<GalleryPost>

    @POST("/api/gallery/posts/delete")
    suspend fun deleteGallery(@Body deleteGallery: Delete) : Response<Unit>

    @POST("/api/gallery/posts/like")
    suspend fun likeGallery(@Body like: Like) : Response<Unit>

    @POST("/api/gallery/posts/unlike")
    suspend fun unlikeGallery(@Body unlike: UnLike) : Response<Unit>

    @POST("/api/gallery/posts/show")
    suspend fun showGallery(@Body show: Show) : Response<GalleryPost>

    @POST("/api/gallery/posts/update")
    suspend fun updateGallery(@Body update: Update) : Response<GalleryPost>

    @POST("/api/i/gallery/posts")
    suspend fun myGalleryPosts(@Body request: GetPosts) : Response<List<GalleryPost>>

    @POST("/api/i/gallery/likes")
    suspend fun likedGalleryPosts(@Body request: GetPosts) : Response<List<LikedGalleryPost>>

    @POST("/api/users/gallery/posts")
    suspend fun userPosts(@Body request: GetPosts) : Response<List<GalleryPost>>
}
open class MisskeyAPIV1275(misskey: MisskeyAPI, private val misskeyAPIV1275Diff: MisskeyAPIV1275Diff, misskeyAPIV12Diff: MisskeyAPIV12Diff, misskeyAPIV11Diff: MisskeyAPIV11Diff) : MisskeyAPIV12(misskey, misskeyAPIV12Diff, misskeyAPIV11Diff),
    MisskeyAPIV1275Diff {

    override suspend fun createGallery(createGallery: CreateGallery): Response<GalleryPost> = misskeyAPIV1275Diff.createGallery(createGallery)

    override suspend fun deleteGallery(deleteGallery: Delete): Response<Unit> = misskeyAPIV1275Diff.deleteGallery(deleteGallery)

    override suspend fun featuredGalleries(i: I): Response<List<GalleryPost>> = misskeyAPIV1275Diff.featuredGalleries(i)

    override suspend fun galleryPosts(getGalleryPost: GetPosts): Response<List<GalleryPost>> = misskeyAPIV1275Diff.galleryPosts(getGalleryPost)

    override suspend fun likeGallery(like: Like): Response<Unit> = misskeyAPIV1275Diff.likeGallery(like)

    override suspend fun popularGalleries(i: I): Response<List<GalleryPost>> = misskeyAPIV1275Diff.popularGalleries(i)

    override suspend fun showGallery(show: Show): Response<GalleryPost> = misskeyAPIV1275Diff.showGallery(show)

    override suspend fun unlikeGallery(unlike: UnLike): Response<Unit> = misskeyAPIV1275Diff.unlikeGallery(unlike)

    override suspend fun updateGallery(update: Update): Response<GalleryPost> = misskeyAPIV1275Diff.updateGallery(update)

    override suspend fun likedGalleryPosts(request: GetPosts): Response<List<LikedGalleryPost>> = misskeyAPIV1275Diff.likedGalleryPosts(request)

    override suspend fun myGalleryPosts(request: GetPosts): Response<List<GalleryPost>> = misskeyAPIV1275Diff.myGalleryPosts(request)

    override suspend fun userPosts(request: GetPosts): Response<List<GalleryPost>> = misskeyAPIV1275Diff.userPosts(request)



}