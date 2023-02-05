package net.pantasystem.milktea.data.infrastructure.gallery

import kotlinx.coroutines.*
import net.pantasystem.milktea.api.misskey.v12_75_0.*
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.GalleryPostDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.drive.FileUploaderProvider
import net.pantasystem.milktea.data.infrastructure.drive.UploadSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.gallery.*
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.instance.IllegalVersionException
import javax.inject.Inject
import net.pantasystem.milktea.api.misskey.v12_75_0.CreateGallery as CreateGalleryDTO


class GalleryRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val galleryDataSource: GalleryDataSource,
    private val fileUploaderProvider: FileUploaderProvider,
    private val accountRepository: AccountRepository,
    private val galleryPostDTOEntityConverter: GalleryPostDTOEntityConverter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : GalleryRepository {


    override suspend fun create(createGalleryPost: CreateGalleryPost): GalleryPost {
        return withContext(ioDispatcher) {
            val files = coroutineScope {
                createGalleryPost.files.map {
                    async {
                        when (it) {
                            is AppFile.Remote -> it.id
                            is AppFile.Local -> {
                                fileUploaderProvider.get(createGalleryPost.author)
                                    .upload(UploadSource.LocalFile(it), true).id
                            }
                        }
                    }
                }.awaitAll()
            }

            val created = getMisskeyAPI(createGalleryPost.author).createGallery(
                CreateGalleryDTO(
                    createGalleryPost.author.token,
                    createGalleryPost.title,
                    createGalleryPost.description,
                    fileIds = files.map {
                        it.fileId
                    },
                    isSensitive = createGalleryPost.isSensitive
                )
            ).throwIfHasError().body()
            requireNotNull(created)

            val gallery = galleryPostDTOEntityConverter.convert(created, createGalleryPost.author)
            galleryDataSource.add(gallery)
            gallery
        }
    }

    override suspend fun delete(id: GalleryPost.Id) {
        withContext(ioDispatcher) {
            val account = accountRepository.get(id.accountId).getOrThrow()
            val res = getMisskeyAPI(account).deleteGallery(
                Delete(
                    i = account.token,
                    postId = id.galleryId
                )
            ).throwIfHasError()
            require(res.isSuccessful)
        }
    }

    override suspend fun find(id: GalleryPost.Id): GalleryPost {
        return withContext(ioDispatcher) {
            try {
                return@withContext galleryDataSource.find(id).getOrThrow()
            } catch (_: GalleryNotFoundException) {

            }
            val account = accountRepository.get(id.accountId).getOrThrow()
            val res = getMisskeyAPI(account).showGallery(
                Show(
                    i = account.token,
                    postId = id.galleryId
                )
            ).throwIfHasError()
            val body = res.body()
            requireNotNull(body)
            val gallery = galleryPostDTOEntityConverter.convert(body, account)
            galleryDataSource.add(gallery)
            gallery
        }
    }

    override suspend fun like(id: GalleryPost.Id) {
        withContext(ioDispatcher) {
            val gallery = find(id) as? GalleryPost.Authenticated
                ?: throw UnauthorizedException()
            val account = accountRepository.get(id.accountId).getOrThrow()
            getMisskeyAPI(account).likeGallery(
                Like(
                    i = account.token,
                    postId = id.galleryId
                )
            ).throwIfHasError()
            galleryDataSource.add(gallery.copy(isLiked = true))
        }
    }

    override suspend fun unlike(id: GalleryPost.Id) {
        withContext(ioDispatcher) {
            val gallery = find(id) as? GalleryPost.Authenticated
                ?: throw UnauthorizedException()
            val account = accountRepository.get(id.accountId).getOrThrow()
            getMisskeyAPI(account).unlikeGallery(
                UnLike(
                    i = account.token,
                    postId = id.galleryId
                )
            ).throwIfHasError()
            galleryDataSource.add(gallery.copy(isLiked = false))
        }
    }

    override suspend fun update(updateGalleryPost: UpdateGalleryPost): GalleryPost {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(updateGalleryPost.id.accountId).getOrThrow()
            val files = coroutineScope {
                updateGalleryPost.files.map {
                    async {
                        when (it) {
                            is AppFile.Remote -> it.id
                            is AppFile.Local -> {
                                fileUploaderProvider.get(account)
                                    .upload(UploadSource.LocalFile(it), true).id
                            }
                        }
                    }
                }.awaitAll()
            }
            val body = getMisskeyAPI(account).updateGallery(
                Update(
                    i = account.token,
                    postId = updateGalleryPost.id.galleryId,
                    description = updateGalleryPost.description,
                    title = updateGalleryPost.title,
                    fileIds = files.map { it.fileId },
                    isSensitive = updateGalleryPost.isSensitive
                )
            ).throwIfHasError().body()
            requireNotNull(body)
            val gallery = galleryPostDTOEntityConverter.convert(body, account)
            galleryDataSource.add(gallery)
            gallery
        }
    }

    private fun getMisskeyAPI(account: Account): MisskeyAPIV1275 {
        return misskeyAPIProvider.get(account.normalizedInstanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
    }
}