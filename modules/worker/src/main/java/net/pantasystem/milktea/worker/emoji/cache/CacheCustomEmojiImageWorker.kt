package net.pantasystem.milktea.worker.emoji.cache

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
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
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.SaveCustomEmojiImageUseCase
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.worker.R
import java.util.concurrent.TimeUnit
import kotlin.math.min

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
        const val WORKER_NAME = "cacheEmojiImages"

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
        setForeground(createForegroundInfo())
        try {
            val hosts = accountRepository.findAll().getOrThrow().map {
                it.getHost()
            }.distinct().shuffled()
            imageCacheRepository.deleteExpiredCaches()
            coroutineScope {
                hosts.forEach {
                    // 全てのインスタンスの絵文字を一括でメモリ上に展開すると、メモリの確保が間に合わずOOMになる可能性があるので、
                    // インスタンスごとにメモリ上に展開されるように実行している
                    val emojis = customEmojiRepository.findBy(it).getOrElse { emptyList() }

                    // 絵文字の個数分並列で実行すると、ネットワークに負荷がかかりすぎるのと、メモリの消費量が大きくなりすぎるので、3分割して実行する
                    val chunkedSize = if (emojis.isEmpty()) 0 else min(emojis.size / 3, emojis.size)
                    emojis.chunked(chunkedSize).map { chunkedEmojis ->
                        async {
                            chunkedEmojis.map { emoji ->
                                saveCustomEmojiImageUseCase(emoji).onFailure { e ->
                                    logger.error("Failed to cache custom emoji image", e)
                                }
                            }
                        }
                    }.awaitAll()
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

        val cancelPendingIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)


        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, applicationContext.getString(android.R.string.cancel), cancelPendingIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(7, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(7, notification)
        }
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