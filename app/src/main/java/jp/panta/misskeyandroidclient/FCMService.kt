package jp.panta.misskeyandroidclient

import android.app.*
import android.content.Context
import android.os.Binder as ABinder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jp.panta.misskeyandroidclient.model.notification.toPushNotification
import jp.panta.misskeyandroidclient.model.sw.register.SubscriptionRegistration
import jp.panta.misskeyandroidclient.viewmodel.MiCore

const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
const val GROUP_KEY_MISSKEY_NOTIFICATION = "jp.panta.misskeyandroidclient.notifications"


class FCMService : FirebaseMessagingService() {

    private var _isActivityActive = false
    private val _binder: Binder by lazy {
        Binder(this)
    }

    override fun onCreate() {
        super.onCreate()

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityStarted(activity: Activity) {
                Log.d("onActivityStarted", "FCMService")
                if(activity is MainActivity) {
                    _isActivityActive = true
                }
            }

            override fun onActivityStopped(activity: Activity) {
                if(activity is MainActivity) {
                    _isActivityActive = false
                }
            }

        })
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

        //if(isCurrentAccountsNotification && _isActivityActive) {
            // 通知がcurrent accountでプッシュ通知の不要なActivityがActiveな時はこれ以上処理をしない
        //    return
        //}
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

    private fun makeNotificationManager(id: String): NotificationManager {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.app_name)
        val description = "THE NOTIFICATION"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager.getNotificationChannel(id) == null){
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
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