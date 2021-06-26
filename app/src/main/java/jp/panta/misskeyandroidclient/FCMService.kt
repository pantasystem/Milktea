package jp.panta.misskeyandroidclient

import android.app.*
import android.content.Context
import android.os.Binder as ABinder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jp.panta.misskeyandroidclient.model.notification.toPushNotification
import jp.panta.misskeyandroidclient.viewmodel.MiCore

const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
const val GROUP_KEY_MISSKEY_NOTIFICATION = "jp.panta.misskeyandroidclient.notifications"


class FCMService : FirebaseMessagingService() {

    private val _binder: Binder by lazy {
        Binder(this)
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Thread{
            runCatching {
                //SubscriptionRegistration()
            }
        }.start()
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

        /*when(pushNotification.type) {
            "follow" -> {

            }
        }*/
        with(makeNotificationManager(NOTIFICATION_CHANNEL_ID)){
            notify(5, builder.build())
            this
        }




    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()

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

    class Binder(private val fcmService: FCMService) : ABinder() {
        fun getService () : FCMService {
            return fcmService
        }
    }

}