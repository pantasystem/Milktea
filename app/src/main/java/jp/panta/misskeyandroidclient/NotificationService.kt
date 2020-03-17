package jp.panta.misskeyandroidclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData
import java.lang.ref.WeakReference
import java.util.*

class NotificationService : Service() {
    companion object{
        private const val TAG = "NotificationService"
        private const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
        private const val MESSAGE_CHANEL_ID = "jp.panta.misskeyandroidclient.NotificationService.MESSAGE_CHANEL_ID"

        const val MSG_NOTIFICATION = 0

        const val MSG_ACTION_CANCEL_NOTIFICATION = 1
        const val MSG_SHOW_NOTIFICATION = 2
        const val MSG_DO_NOT_SHOW_NOTIFICATION = 3
    }
    private val mGson = GsonBuilder().create()
    private lateinit var mClientMessageHandler: ClientMessageHandler
    private lateinit var mMessenger: Messenger

    private var mNotificationManager: NotificationManager? = null

    var isShowNotification: Boolean = true

    override fun onBind(intent: Intent): IBinder? {
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startObserve()
        Log.d(TAG, "serviceを開始した")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        mClientMessageHandler = ClientMessageHandler(this)
        mMessenger = Messenger(mClientMessageHandler)

    }

    private fun startObserve(){

        (applicationContext as MiApplication).accounts.observeForever {accountRelations ->
            accountRelations?.forEach{ar ->
                Log.d(TAG, "observerを登録しています")

                ar.getCurrentConnectionInformation()?.let{ ci ->
                    val adapter = StreamingAdapter(ci, (application as MiApplication).getEncryption())
                    adapter.connect()
                    val mainCapture = MainCapture(mGson)
                    mainCapture.putListener(MainChannelObserver(ar.account))
                    val id = UUID.randomUUID().toString()
                    adapter.addObserver(id, mainCapture)
                }

            }
        }
    }

    private inner class MainChannelObserver(
        val account: Account
    ) : MainCapture.AbsListener(){
        override fun notification(notification: Notification) {
            Handler(Looper.getMainLooper()).post{
                Log.d(TAG, "notification,:$notification")
                //val miApplication = applicationContext as MiApplication
                if(isShowNotification){
                    showNotification(NotificationViewData(notification, account))
                }
            }
        }

        override fun messagingMessage(message: Message) {
            Handler(Looper.getMainLooper()).post{
                Log.d(TAG, "message: $message")
                if(account.id != message.userId){
                    showMessageNotification(message)
                }
            }
        }
    }

    private fun showNotification(notification: NotificationViewData){
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            builder.setSmallIcon(R.mipmap.ic_launcher)
            when(notification.type){
                NotificationViewData.Type.FOLLOW ->{
                    //builder.setSmallIcon(R.drawable.ic_follow)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.followed_by))
                }
                NotificationViewData.Type.MENTION ->{
                    //builder.setSmallIcon(R.drawable.ic_mention)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.mention_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.text))
                }
                NotificationViewData.Type.REPLY ->{
                    //builder.setSmallIcon(R.drawable.ic_reply_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.replied_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.text))
                }
                NotificationViewData.Type.QUOTE ->{
                    //builder.setSmallIcon(R.drawable.ic_format_quote_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.quoted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.toShowNote?.text))
                }
                NotificationViewData.Type.POLL_VOTE->{
                    //builder.setSmallIcon(R.drawable.ic_poll_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.voted_by))
                }
                NotificationViewData.Type.REACTION ->{
                    //builder.setSmallIcon(R.drawable.ic_reaction_like)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.reacted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.reaction))
                }
                NotificationViewData.Type.RENOTE ->{
                    // builder.setSmallIcon(R.drawable.ic_re_note)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.renoted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.toShowNote?.text))
                }
                NotificationViewData.Type.RECEIVE_FOLLOW_REQUEST ->{
                    /*
                    E/AndroidRuntime: FATAL EXCEPTION: main
    Process: jp.panta.misskeyandroidclient, PID: 27540
    java.lang.IllegalArgumentException: Invalid notification (no valid small icon): Notification(channel=jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x0 color=0x00000000 vis=PRIVATE)
        at android.app.NotificationManager.fixNotification(NotificationManager.java:519)
        at android.app.NotificationManager.notifyAsUser(NotificationManager.java:498)
        at android.app.NotificationManager.notify(NotificationManager.java:447)
        at android.app.NotificationManager.notify(NotificationManager.java:423)
        at jp.panta.misskeyandroidclient.NotificationService.showNotification(NotificationService.kt:134)
        at jp.panta.misskeyandroidclient.NotificationService.access$showNotification(NotificationService.kt:28)
        at jp.panta.misskeyandroidclient.NotificationService$MainChannelObserver$notification$1.run(NotificationService.kt:70)
        at android.os.Handler.handleCallback(Handler.java:883)
        at android.os.Handler.dispatchMessage(Handler.java:100)
        at android.os.Looper.loop(Looper.java:224)
        at android.app.ActivityThread.main(ActivityThread.java:7520)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:539)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:950)
E/MQSEventManagerDelegate: failed to get MQSService.
                     */
                    //builder.setSmallIcon(R.drawable.ic_supervisor_account_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.request_follow))
                    //

                    // 通知をタップした時の動作
                    // PendingIntent.getBroadcast()
                    // builder.setContentIntent()
                    return

                }
                else ->{
                    Log.d("NotificationService", "unknown notification: $notification")
                    return
                }
                /*else ->{
                    builder.setSmallIcon(R.mipmap.ic_launcher)

                }*/

            }
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        mNotificationManager = with(makeNotificationManager(NOTIFICATION_CHANNEL_ID)){
            notify(5, builder.build())
            this
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

    private fun cancelAllNotification(){
        mNotificationManager?.cancelAll()
    }

    private fun sendNotificationToClient(notification: Notification){
        val message = Message()
        message.obj = notification
        message.what = MSG_NOTIFICATION
        mMessenger.send(message)
    }


    private class ClientMessageHandler(service: NotificationService) : Handler(){

        private val mService: WeakReference<NotificationService> = WeakReference(service)

        // client（Activity）などからメッセージを受信したとき
        override fun handleMessage(msg: android.os.Message?) {
            super.handleMessage(msg)

            when(msg?.what){
                MSG_ACTION_CANCEL_NOTIFICATION ->{
                    mService.get()?.cancelAllNotification()
                }
                MSG_DO_NOT_SHOW_NOTIFICATION ->{
                    mService.get()?.isShowNotification = false
                }
                MSG_SHOW_NOTIFICATION ->{
                    mService.get()?.isShowNotification = true
                }
            }

            //msg?.replyTo?.send()

        }
    }

}
