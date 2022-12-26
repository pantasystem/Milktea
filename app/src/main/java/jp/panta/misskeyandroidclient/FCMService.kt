package jp.panta.misskeyandroidclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notification.PushNotification
import net.pantasystem.milktea.model.notification.toPushNotification
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.user.activity.UserDetailActivity
import net.pantasystem.milktea.worker.sw.SubscriptionRegistrationWorker
import javax.inject.Inject

const val NOTIFICATION_CHANNEL_ID =
    "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
const val GROUP_KEY_MISSKEY_NOTIFICATION = "jp.panta.misskeyandroidclient.notifications"


@Suppress("SameParameterValue")
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {


    @Inject lateinit var accountStore: AccountStore


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val subscriptionRegistrationWorker =
            OneTimeWorkRequestBuilder<SubscriptionRegistrationWorker>()

                .setInputData(
                    workDataOf(
                        SubscriptionRegistrationWorker.TOKEN to token
                    )
                ).build()

        WorkManager.getInstance(this).enqueue(subscriptionRegistrationWorker)
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)

        // receive message
        val pushNotification = msg.data.toPushNotification()
        val isCurrentAccountsNotification =
            accountStore.currentAccountId == pushNotification.accountId

        if (isCurrentAccountsNotification) {
            // 通知がcurrent accountでプッシュ通知の不要なActivityがActiveな時はこれ以上処理をしない
            return
        }
        Log.d("FCMService", "pushNotification:$pushNotification")
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(pushNotification.title)
            .setContentText(pushNotification.body)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setGroup(GROUP_KEY_MISSKEY_NOTIFICATION)
            .setGroupSummary(true)

        runCancellableCatching {
            val pendingIntentBuilder = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(pushNotification.makeIntent())
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntentBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
            } else {
                pendingIntentBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            builder.setContentIntent(pendingIntent)
        }.onFailure { e ->
            Log.e("FCMService", "Intent作成に失敗", e)
            throw e
        }

        with(makeNotificationManager(NOTIFICATION_CHANNEL_ID)) {
            notify(5, builder.build())
            this
        }


    }

//    override fun onDeletedMessages() {
//        super.onDeletedMessages()
//
//    }

    private fun PushNotification.makeIntent(): Intent {
        return when (this.type) {
            "follow", "receiveFollowRequest", "followRequestAccepted" -> UserDetailActivity.newInstance(
                this@FCMService,
                User.Id(accountId, this.userId!!)
            ).apply {
                putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)

            }
            "mention", "reply", "renote", "quote", "reaction" -> NoteDetailActivity.newIntent(
                this@FCMService,
                Note.Id(accountId, noteId!!)
            ).apply {
                putExtra(NoteDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
            }
            else -> Intent(this@FCMService, MainActivity::class.java)
        }
    }

    private fun makeNotificationManager(channelId: String): NotificationManager {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.app_name)
        val description = "THE NOTIFICATION"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel =
                    NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
                channel.description = description
                notificationManager.createNotificationChannel(channel)
            }
        }
        return notificationManager

    }


}