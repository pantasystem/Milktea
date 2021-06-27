package jp.panta.misskeyandroidclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notification.PushNotification
import jp.panta.misskeyandroidclient.model.notification.toPushNotification
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.workers.SubscriptionRegistrationWorker

const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
const val GROUP_KEY_MISSKEY_NOTIFICATION = "jp.panta.misskeyandroidclient.notifications"


class FCMService : FirebaseMessagingService() {



    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val subscriptionRegistrationWorker = OneTimeWorkRequestBuilder<SubscriptionRegistrationWorker>()
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
        val isCurrentAccountsNotification = (application as? MiCore)?.getCurrentAccount()?.value?.accountId == pushNotification.accountId

        if(isCurrentAccountsNotification) {
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

        runCatching {
            val pendingIntent = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(pushNotification.makeIntent())
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            builder.setContentIntent(pendingIntent)
        }.onFailure { e ->
            Log.e("FCMService", "Intent作成に失敗", e)
            if(BuildConfig.DEBUG) {
                Toast.makeText(this, "Intent作成処理に失敗:${pushNotification}, e:${e}", Toast.LENGTH_LONG).show()
                throw e
            }
        }

        with(makeNotificationManager(NOTIFICATION_CHANNEL_ID)){
            notify(5, builder.build())
            this
        }




    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()

    }

    private fun PushNotification.makeIntent() : Intent {
        return when(this.type) {
            "follow", "receiveFollowRequest",  "followRequestAccepted" -> UserDetailActivity.newInstance(this@FCMService, User.Id(accountId, this.userId!!)).apply {
                putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)

            }
            "mention", "reply", "renote", "quote", "reaction" -> NoteDetailActivity.newIntent(this@FCMService, Note.Id(accountId, noteId!!)).apply {
                putExtra(NoteDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
            }
            else -> Intent(this@FCMService, MainActivity::class.java)
        }
    }

    private fun makeNotificationManager(channelId: String): NotificationManager {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.app_name)
        val description = "THE NOTIFICATION"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager.getNotificationChannel(channelId) == null){
                val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
                channel.description = description
                notificationManager.createNotificationChannel(channel)
            }
        }
        return notificationManager

    }



}