package net.pantasystem.milktea.data.infrastructure.image

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.BoxStore
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.inValues
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.common.Hash
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.image.ImageCache
import net.pantasystem.milktea.model.image.ImageCacheRepository
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class ImageCacheRepositoryImpl @Inject constructor(
    private val boxStore: BoxStore,
    private val okHttpClientProvider: OkHttpClientProvider,
    @ApplicationContext val context: Context,
    @IODispatcher val coroutineDispatcher: CoroutineDispatcher,
) : ImageCacheRepository {

    companion object {
        const val cacheDir = "milktea_image_caches"
        val cacheExpireDuration = 7.days
        val cacheIgnoreUpdateDuration = 3.days
    }

    private val imageCacheStore by lazy {
        boxStore.boxFor(ImageCacheRecord::class.java)
    }

    override suspend fun save(url: String): ImageCache {
        when(val cache = findBySourceUrl(url)) {
            null -> Unit
            else -> if (cache.cachedAt + cacheIgnoreUpdateDuration > Clock.System.now()) {
                return cache
            }
        }
        return withContext(coroutineDispatcher) {
            val req = Request.Builder().url(url).build()
            okHttpClientProvider.get().newCall(req)
            val fileName = Hash.sha256(url)
            File(context.cacheDir, cacheDir).apply {
                if (!exists()) {
                    mkdirs()
                }
            }.resolve(fileName).outputStream().use { out ->
                okHttpClientProvider.get().newCall(req).execute().body?.byteStream()
                    ?.use { inStream ->
                        inStream.copyTo(out)
                    }
            }
            val cache = ImageCache(
                sourceUrl = url,
                cachePath = File(context.cacheDir, cacheDir).resolve(fileName).absolutePath,
                cachedAt = Clock.System.now()
            )
            val record = ImageCacheRecord.from(
                cache
            )
            boxStore.awaitCallInTx {
                val existsRecord = imageCacheStore.query().equal(
                    ImageCacheRecord_.sourceUrl,
                    url,
                    QueryBuilder.StringOrder.CASE_SENSITIVE
                ).build().findFirst()
                if (existsRecord == null) {
                    imageCacheStore.put(record)
                } else {
                    existsRecord.applyModel(record.toModel())
                    imageCacheStore.put(existsRecord)
                }
            }
            return@withContext cache
        }
    }

    override suspend fun findBySourceUrl(url: String): ImageCache? {
        return withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            val record = imageCacheStore.query().equal(
                ImageCacheRecord_.sourceUrl,
                url,
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().findFirst()
            val model = record?.toModel()
            if (model != null && now - model.cachedAt > cacheExpireDuration) {
                imageCacheStore.remove(record)
                null
            } else {
                 model
            }
        }
    }

    override suspend fun deleteExpiredCaches() {
        withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            imageCacheStore.query().lessOrEqual(
                ImageCacheRecord_.cachedAt,
                now.toEpochMilliseconds()
            ).build().remove()
        }
    }

    override suspend fun clear() {
        withContext(coroutineDispatcher) {
            imageCacheStore.removeAll()
            File(context.cacheDir, cacheDir).deleteRecursively()
        }
    }

    override suspend fun findBySourceUrls(urls: List<String>): List<ImageCache> {
        return withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            val records = imageCacheStore.query().inValues(
                ImageCacheRecord_.sourceUrl,
                urls.toTypedArray(),
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().find()
            records.mapNotNull { record ->
                val model = record.toModel()
                if (now - model.cachedAt > cacheExpireDuration) {
                    null
                } else {
                    model
                }
            }
        }
    }
}