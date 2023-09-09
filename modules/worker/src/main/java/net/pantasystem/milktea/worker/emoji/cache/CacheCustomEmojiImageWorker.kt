package net.pantasystem.milktea.worker.emoji.cache

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
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
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.SaveCustomEmojiImageUseCase
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.worker.R
import java.util.concurrent.TimeUnit

@HiltWorker
class CacheCustomEmojiImageWorker @AssistedInject constructor(
    private val accountRepository: AccountRepository,
    private val customEmojiRepository: CustomEmojiRepository,
    private val imageCacheRepository: ImageCacheRepository,
    private val saveCustomEmojiImageUseCase: SaveCustomEmojiImageUseCase,
    private val loggerFactory: Logger.Factory,
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "CACHE_CUSTOM_EMOJI_IMAGE_WORKER"

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
            setForeground(createForegroundInfo())
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
                                    saveCustomEmojiImageUseCase(emoji).onFailure {
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

    private fun createForegroundInfo(): ForegroundInfo {
        val title = applicationContext.getString(R.string.notification_sync_download_custom_emoji_title)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, applicationContext.getString(android.R.string.cancel), null)
            .build()

        return ForegroundInfo(7, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create a Notification channel
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, "Custom emoji download notification", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Notification representing custom emoji download"
            notificationManager.createNotificationChannel(channel)
        }
    }


}