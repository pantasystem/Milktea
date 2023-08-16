package net.pantasystem.milktea.worker.emoji.cache

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.image.ImageCacheRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class CacheCustomEmojiImageWorker @AssistedInject constructor(
    private val accountRepository: AccountRepository,
    private val customEmojiRepository: CustomEmojiRepository,
    private val imageCacheRepository: ImageCacheRepository,
    private val loggerFactory: Logger.Factory,
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi (or Ethernet etc) required
                .build()

            return PeriodicWorkRequestBuilder<CacheCustomEmojiImageWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
        }
    }

    private val logger by lazy {
        loggerFactory.create("CacheCustomEmojiImageWorker")
    }

    override suspend fun doWork(): Result {
        try {
            val hosts = accountRepository.findAll().getOrThrow().map {
                it.getHost()
            }.distinct()
            imageCacheRepository.deleteExpiredCaches()
            coroutineScope {
                hosts.mapNotNull {
                    customEmojiRepository.findBy(it).getOrNull()
                }.map { emojis ->
                    launch {
                        emojis.chunked(if (emojis.isEmpty()) 0 else ( emojis.size / 4)).forEach { emojis ->
                            emojis.mapNotNull { emoji ->
                                (emoji.url ?: emoji.uri)?.let {
                                    runCancellableCatching {
                                        imageCacheRepository.save(it)
                                    }.onFailure {
                                        logger.error(
                                            "Failed to cache emoji image: ${emoji.url ?: emoji.uri}",
                                            it
                                        )
                                    }.getOrNull()
                                }
                            }
                        }
                    }
                    delay(1000)
                }
            }


            return Result.success()
        } catch (e: Exception) {
            logger.error("Failed to cache custom emoji images", e)
            return Result.failure()
        }
    }

}