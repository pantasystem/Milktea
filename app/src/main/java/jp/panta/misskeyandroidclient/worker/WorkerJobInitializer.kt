package jp.panta.misskeyandroidclient.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.pantasystem.milktea.worker.SyncAccountInfoWorker
import net.pantasystem.milktea.worker.SyncNodeInfoCacheWorker
import net.pantasystem.milktea.worker.drive.CleanupUnusedDriveCacheWorker
import net.pantasystem.milktea.worker.emoji.cache.CacheCustomEmojiImageWorker
import net.pantasystem.milktea.worker.filter.SyncMastodonFilterWorker
import net.pantasystem.milktea.worker.meta.SyncMetaWorker
import net.pantasystem.milktea.worker.sw.RegisterAllSubscriptionRegistration
import net.pantasystem.milktea.worker.user.SyncLoggedInUserInfoWorker
import net.pantasystem.milktea.worker.user.renote.mute.SyncRenoteMutesWorker
import javax.inject.Inject

class WorkerJobInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    operator fun invoke() {
        WorkManager.getInstance(context).apply {
            enqueue(RegisterAllSubscriptionRegistration.createWorkRequest())
            enqueue(CleanupUnusedDriveCacheWorker.createOneTimeRequest())
            enqueueUniquePeriodicWork(
                "syncMeta",
                ExistingPeriodicWorkPolicy.REPLACE,
                SyncMetaWorker.createPeriodicWorkRequest()
            )
            enqueueUniquePeriodicWork(
                "syncNodeInfos",
                ExistingPeriodicWorkPolicy.UPDATE,
                SyncNodeInfoCacheWorker.createPeriodicWorkRequest()
            )
            enqueueUniquePeriodicWork(
                "syncLoggedInUsers",
                ExistingPeriodicWorkPolicy.UPDATE,
                SyncLoggedInUserInfoWorker.createPeriodicWorkRequest(),
            )
            enqueueUniquePeriodicWork(
                "syncAccountInfo",
                ExistingPeriodicWorkPolicy.UPDATE,
                SyncAccountInfoWorker.createPeriodicWorkRequest(),
            )

            enqueueUniquePeriodicWork(
                "syncMastodonWordFilter",
                ExistingPeriodicWorkPolicy.UPDATE,
                SyncMastodonFilterWorker.createPeriodicWorkerRequest(),
            )
            enqueueUniquePeriodicWork(
                CacheCustomEmojiImageWorker.WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                CacheCustomEmojiImageWorker.createPeriodicWorkRequest(),
            )

            enqueue(
                SyncRenoteMutesWorker.createOneTimeWorkRequest()
            )
        }

    }
}