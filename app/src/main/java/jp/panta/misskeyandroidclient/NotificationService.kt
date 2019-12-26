package jp.panta.misskeyandroidclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData
import java.util.*

class NotificationService : Service() {
    companion object{
        private const val TAG = "NotificationService"
        private const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
        private const val MESSAGE_CHANEL_ID = "jp.panta.misskeyandroidclient.NotificationService.MESSAGE_CHANEL_ID"
    }
    private val mGson = GsonBuilder().create()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startObserve()
        Log.d(TAG, "serviceを開始した")
        return START_STICKY
    }


    private fun startObserve(){

        (applicationContext as MiApplication).connectionInstancesLiveData.observeForever {connectionInstances ->
            connectionInstances.forEach{ci ->
                Log.d(TAG, "observerを登録しています")

                val adapter = StreamingAdapter(ci, (application as MiApplication).encryption)
                adapter.connect()
                val mainCapture = MainCapture(ci, mGson)
                mainCapture.addListener(MainChannelObserver(ci))
                val id = UUID.randomUUID().toString()
                adapter.addObserver(id, mainCapture)
            }
        }
    }

    private inner class MainChannelObserver(
        val connectionInstance: ConnectionInstance
    ) : MainCapture.AbsListener(){
        override fun notification(notification: Notification) {
            Handler(Looper.getMainLooper()).post{
                Log.d(TAG, "notification,:$notification")
                //val miApplication = applicationContext as MiApplication
                showNotification(NotificationViewData(notification, connectionInstance))
            }
        }

        override fun messagingMessage(message: Message) {
            Handler(Looper.getMainLooper()).post{
                Log.d(TAG, "message: $message")
                if(connectionInstance.userId != message.userId){
                    showMessageNotification(message)
                }
            }
        }
    }

    private fun showNotification(notification: NotificationViewData){
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            //.setSmallIcon(R.mipmap.ic_launcher)
            when(notification.type){
                NotificationViewData.Type.FOLLOW ->{
                    builder.setSmallIcon(R.drawable.ic_follow)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.followed_by))
                }
                NotificationViewData.Type.MENTION ->{
                    builder.setSmallIcon(R.drawable.ic_mention)
                    builder.setContentTitle(notification.user.getDisplayUserName())
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.text))
                }
                NotificationViewData.Type.REPLY ->{
                    builder.setSmallIcon(R.drawable.ic_reply_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.replied_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.text))
                }
                NotificationViewData.Type.QUOTE ->{
                    builder.setSmallIcon(R.drawable.ic_format_quote_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.quoted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.toShowNote?.text))
                }
                NotificationViewData.Type.POLL_VOTE->{
                    builder.setSmallIcon(R.drawable.ic_poll_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.voted_by))
                }
                NotificationViewData.Type.REACTION ->{
                    builder.setSmallIcon(R.drawable.ic_reaction_like)
                    builder.setContentTitle(notification.user.getDisplayUserName())
                    builder.setContentText(SafeUnbox.unbox(notification.reaction))
                }
                NotificationViewData.Type.RENOTE ->{
                    builder.setSmallIcon(R.drawable.ic_re_note)
                    builder.setContentTitle(notification.user.getDisplayUserName())
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.toShowNote?.text))
                }
                NotificationViewData.Type.RECEIVE_FOLLOW_REQUEST ->{
                    builder.setSmallIcon(R.drawable.ic_supervisor_account_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.request_follow))

                }
                /*else ->{
                    builder.setSmallIcon(R.mipmap.ic_launcher)

                }*/

            }
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        with(makeNotificationManager(NOTIFICATION_CHANNEL_ID)){
            notify(5, builder.build())
        }

    }

    private fun showMessageNotification(message: Message){

        val builder = NotificationCompat.Builder(this, MESSAGE_CHANEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(message.user?.getDisplayUserName())
            .setContentText(SafeUnbox.unbox(message.text))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        with(makeNotificationManager(MESSAGE_CHANEL_ID)){
            notify(6, builder.build())
        }
    }

    private fun makeNotificationManager(id: String): NotificationManager{
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

}
