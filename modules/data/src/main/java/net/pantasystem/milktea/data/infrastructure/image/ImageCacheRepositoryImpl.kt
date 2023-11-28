package net.pantasystem.milktea.data.infrastructure.image

import android.content.Context
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.inValues
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.common.Hash
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.BoxStoreHolder
import net.pantasystem.milktea.model.image.ImageCache
import net.pantasystem.milktea.model.image.ImageCacheRepository
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.days


class ImageCacheRepositoryImpl @Inject constructor(
    private val boxStoreHolder: BoxStoreHolder,
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
        boxStoreHolder.boxStore.boxFor(ImageCacheRecord::class.java)
    }

    override suspend fun save(url: String): ImageCache {
        when (val cache = findBySourceUrl(url)) {
            null -> Unit
            else -> if (cache.cachedAt + cacheIgnoreUpdateDuration > Clock.System.now()) {
                if (File(cache.cachePath).exists()) {
                    return cache
                }
            }
        }
        return withContext(coroutineDispatcher) {
            val fileName = Hash.sha256(url)
            val file = File(context.filesDir, cacheDir).apply {
                if (!exists()) {
                    mkdirs()
                }
            }.resolve(fileName)

            val (width, height) = downloadAndSaveFile(url, file)

            val cache = ImageCache(
                sourceUrl = url,
                cachePath = File(context.filesDir, cacheDir).resolve(fileName).absolutePath,
                cachedAt = Clock.System.now(),
                width = width,
                height = height,
            )
            upInsert(cache)
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
            val query = imageCacheStore.query().lessOrEqual(
                ImageCacheRecord_.cachedAt,
                (now - cacheExpireDuration).toEpochMilliseconds()
            ).build()

            val deleteCacheSize = query.count()
            for (i in 0 until deleteCacheSize step 100) {
                val list = query.find(i, 100)
                list.forEach {
                    val file = File(it.cachePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                imageCacheStore.remove(list)
            }

        }
    }

    override suspend fun clear() {
        withContext(coroutineDispatcher) {
            imageCacheStore.removeAll()
            File(context.filesDir, cacheDir).deleteRecursively()
        }
    }

    override suspend fun findBySourceUrls(urls: List<String>): List<ImageCache> {
        return withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            urls.chunked(100) { urls ->
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
        }.flatten()
    }

    private suspend fun upInsert(cache: ImageCache) {
        val record = ImageCacheRecord.from(
            cache
        )
        boxStoreHolder.boxStore.awaitCallInTx {
            val existsRecord = imageCacheStore.query().equal(
                ImageCacheRecord_.sourceUrl,
                cache.sourceUrl,
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().findFirst()
            if (existsRecord == null) {
                imageCacheStore.put(record)
            } else {
                existsRecord.applyModel(record.toModel())
                imageCacheStore.put(existsRecord)
            }
        }
    }

    private fun downloadAndSaveFile(url: String, file: File): Pair<Int?, Int?> {
        return file.outputStream().use { out ->
            val req = Request.Builder().url(url).build()
            val response = okHttpClientProvider.get().newCall(req).execute()
            val contentLength = response.header("Content-Length")?.toLongOrNull()
            response.body?.byteStream()
                ?.use { inStream ->
                    val bytesCopied = inStream.copyTo(out)
                    if (contentLength != null && bytesCopied != contentLength) {
                        throw Exception("Download failed: url=$url")
                    }
                }
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(
                file.absolutePath, options
            ) ?: return@use null to null
            options.outWidth to options.outHeight
        }
    }

    override suspend fun findCachedFileCount(reality: Boolean): Long {
        return if (reality) {
            val dir = File(context.filesDir, cacheDir).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            dir.listFiles()?.size?.toLong() ?: 0L
        } else {
            imageCacheStore.count()
        }
    }
}