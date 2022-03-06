package jp.panta.misskeyandroidclient.model.gallery.impl

import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.v12_75_0.*
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.IllegalVersionException
import jp.panta.misskeyandroidclient.model.account.UnauthorizedException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.drive.FileUploaderProvider
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.gallery.*
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import jp.panta.misskeyandroidclient.api.misskey.v12_75_0.CreateGallery as CreateGalleryDTO

fun MiCore.createGalleryRepository() : GalleryRepository{
    return GalleryRepositoryImpl(
        getMisskeyAPIProvider(),
        getGalleryDataSource(),
        getEncryption(),
        getFileUploaderProvider(),
        getUserDataSource(),
        getFilePropertyDataSource(),
        getAccountRepository(),
    )
}

class GalleryRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val galleryDataSource: GalleryDataSource,
    private val encryption: Encryption,
    private val fileUploaderProvider: FileUploaderProvider,
    private val userDataSource: UserDataSource,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val accountRepository: AccountRepository
) : GalleryRepository{


    override suspend fun create(createGalleryPost: CreateGalleryPost): GalleryPost {
        val files = coroutineScope {
            createGalleryPost.files.map {
                async {
                    when(it) {
                        is AppFile.Remote -> it.id
                        is AppFile.Local -> {
                            fileUploaderProvider.get(createGalleryPost.author).upload(it, true).let {
                                it.toFileProperty(createGalleryPost.author).also { entity ->
                                    filePropertyDataSource.add(entity)
                                }
                            }.id
                        }
                    }
                }
            }.awaitAll()
        }

        val created = getMisskeyAPI(createGalleryPost.author).createGallery(
            CreateGalleryDTO(
                createGalleryPost.author.getI(encryption),
                createGalleryPost.title,
                createGalleryPost.description,
                fileIds = files.map {
                    it.fileId
                },
                isSensitive = createGalleryPost.isSensitive
            )
        ).throwIfHasError().body()
        requireNotNull(created)

        val gallery = created.toEntity(createGalleryPost.author, filePropertyDataSource, userDataSource)
        galleryDataSource.add(gallery)
        return gallery
    }

    override suspend fun delete(id: GalleryPost.Id) {
        val account = accountRepository.get(id.accountId)
        val res = getMisskeyAPI(account).deleteGallery(Delete(i = account.getI(encryption), postId = id.galleryId)).throwIfHasError()
        require(res.isSuccessful)
    }

    override suspend fun find(id: GalleryPost.Id): GalleryPost {
        try {
            return galleryDataSource.find(id)
        }catch (e: GalleryNotFoundException) {

        }
        val account = accountRepository.get(id.accountId)
        val res = getMisskeyAPI(account).showGallery(Show(i = account.getI(encryption), postId = id.galleryId)).throwIfHasError()
        val body = res.body()
        requireNotNull(body)
        val gallery = body.toEntity(account, filePropertyDataSource, userDataSource)
        galleryDataSource.add(gallery)
        return gallery
    }

    override suspend fun like(id: GalleryPost.Id) {
        val gallery = find(id) as? GalleryPost.Authenticated
            ?: throw UnauthorizedException()
        val account = accountRepository.get(id.accountId)
        getMisskeyAPI(account).likeGallery(Like(i = account.getI(encryption), postId = id.galleryId)).throwIfHasError()
        galleryDataSource.add(gallery.copy(isLiked = true))
    }

    override suspend fun unlike(id: GalleryPost.Id) {
        val gallery = find(id) as? GalleryPost.Authenticated
            ?: throw UnauthorizedException()
        val account = accountRepository.get(id.accountId)
        getMisskeyAPI(account).unlikeGallery(UnLike(i = account.getI(encryption), postId = id.galleryId)).throwIfHasError()
        galleryDataSource.add(gallery.copy(isLiked = false))

    }

    override suspend fun update(updateGalleryPost: UpdateGalleryPost): GalleryPost {
        val account = accountRepository.get(updateGalleryPost.id.accountId)
        val files = coroutineScope {
            updateGalleryPost.files.map {
                async {
                    when(it) {
                        is AppFile.Remote -> it.id.fileId
                        is AppFile.Local -> {
                            fileUploaderProvider.get(account).upload(it, true).also {
                                filePropertyDataSource.add(it.toFileProperty(account))
                            }.id
                        }
                    }
                }
            }.awaitAll()
        }
        val body = getMisskeyAPI(account).updateGallery(
            Update(
                i = account.getI(encryption),
                postId = updateGalleryPost.id.galleryId,
                description = updateGalleryPost.description,
                title = updateGalleryPost.title,
                fileIds = files,
                isSensitive = updateGalleryPost.isSensitive
            )
        ).throwIfHasError().body()
        requireNotNull(body)
        val gallery = body.toEntity(account, filePropertyDataSource, userDataSource)
        galleryDataSource.add(gallery)
        return gallery
    }

    private fun getMisskeyAPI(account: Account) : MisskeyAPIV1275 {
        return misskeyAPIProvider.get(account.instanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
    }
}