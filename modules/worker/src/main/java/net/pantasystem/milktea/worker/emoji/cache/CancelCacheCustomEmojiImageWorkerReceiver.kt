package net.pantasystem.milktea.worker.emoji.cache

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager

class CancelCacheCustomEmojiImageWorkerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        WorkManager.getInstance(context).cancelUniqueWork(CacheCustomEmojiImageWorker.WORKER_NAME)
    }
}