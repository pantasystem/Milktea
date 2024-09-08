package net.pantasystem.milktea.data.infrastructure.image

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.common.Hash
import net.pantasystem.milktea.common.glide.svg.SvgDecoder
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.image.ImageCache
import net.pantasystem.milktea.model.image.ImageCacheRepository
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.days


class ImageCacheRepositoryImpl @Inject constructor(
    private val okHttpClientProvider: OkHttpClientProvider,
    @ApplicationContext val context: Context,
    @IODispatcher val coroutineDispatcher: CoroutineDispatcher,
    private val imageCacheDAO: ImageCacheDAO,
) : ImageCacheRepository {

    companion object {
        const val cacheDir = "milktea_image_caches"
        val cacheExpireDuration = 7.days
        val cacheIgnoreUpdateDuration = 3.days
    }

    override suspend fun save(url: String) = runCancellableCatching {
        when (val cache = findBySourceUrl(url).getOrThrow()) {
            null -> Unit
            else -> if (cache.cachedAt + cacheIgnoreUpdateDuration > Clock.System.now()) {
                if (File(cache.cachePath).exists()) {
                    return@runCancellableCatching cache
                }
            }
        }
        withContext(coroutineDispatcher) {
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

    override suspend fun findBySourceUrl(url: String): Result<ImageCache?> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            imageCacheDAO.findBySourceUrl(url, (now - cacheExpireDuration).toEpochMilliseconds())?.let {
                it.toModel()
            }
        }
    }

    override suspend fun deleteExpiredCaches(): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            val targets = imageCacheDAO.findOlder((now - cacheExpireDuration).toEpochMilliseconds())
            targets.forEach {
                imageCacheDAO.deleteByUrl(it.sourceUrl)
                val file = File(it.cachePath)
                if (file.exists()) {
                    file.delete()
                }
                imageCacheDAO.deleteByUrl(it.sourceUrl)
            }
        }
    }

    override suspend fun clear(): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            imageCacheDAO.clear()
            File(context.filesDir, cacheDir).deleteRecursively()
        }
    }

    override suspend fun findBySourceUrls(urls: List<String>): Result<List<ImageCache>> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val now = Clock.System.now()
            urls.chunked(300).map { list ->
                imageCacheDAO.findBySourceUrls(
                    list,
                    (now - cacheExpireDuration).toEpochMilliseconds(),
                )
            }
        }.flatten().map { it.toModel() }
    }

    private suspend fun upInsert(cache: ImageCache) {
        imageCacheDAO.upsert(ImageCacheEntity.from(cache))
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

            // check svg
            val source = file.inputStream()
            source.use {
                if (SvgDecoder.isSvg(file.inputStream())) {
                    try {
                        val svg = SVG.getFromInputStream(source)
                        Log.d("ImageCache", "SVGのサイズ: width=${svg.documentWidth}, height=${svg.documentHeight}")
                        svg.documentWidth.toInt() to svg.documentHeight.toInt()
                    } catch (e: SVGParseException) {
                        Log.e("ImageCache", "SVGParseException", e)
                        throw e
                    }
                } else {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(
                        file.absolutePath, options
                    ) ?: return null to null
                    options.outWidth to options.outHeight
                }
            }
        }
    }

    override suspend fun findCachedFileCount(reality: Boolean): Result<Long> = runCancellableCatching {
        if (reality) {
            val dir = File(context.filesDir, cacheDir).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            dir.listFiles()?.size?.toLong() ?: 0L
        } else {
            imageCacheDAO.count()
        }
    }
}