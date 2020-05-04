package jp.panta.misskeyandroidclient

import android.app.*
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
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData.Type.*

class NotificationService : Service() {
    companion object{
        private const val TAG = "NotificationService"
        private const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
        private const val MESSAGE_CHANEL_ID = "jp.panta.misskeyandroidclient.NotificationService.MESSAGE_CHANEL_ID"
        private const val GROUP_KEY = "NotificationService.GROUP_KEY"

        const val SUBSCRIBE_ALL_NOTIFICATIONS = 0
        const val START_PUSH_NOTIFICATION = 4
        const val STOP_PUSH_NOTIFICATION = 5
    }
    private val mGson = GsonBuilder().create()
    //private lateinit var mClientMessageHandler: ClientMessageHandler
    //private lateinit var mMessenger: Messenger

    private lateinit var mBinder: NotificationBinder
    private var mNotificationManager: NotificationManager? = null

    var isShowNotification: Boolean = true

    private val mStopNotificationAccountMap = HashMap<String, Account>()

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startObserve()
        Log.d(TAG, "serviceを開始した")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        //mClientMessageHandler = ClientMessageHandler(this)
        //mMessenger = Messenger(mClientMessageHandler)
        mBinder = NotificationBinder()
    }

    private fun startObserve(){

        (applicationContext as MiApplication).accounts.observeForever {accountRelations ->
            accountRelations?.forEach{ar ->
                Log.d(TAG, "observerを登録しています")

                ar.getCurrentConnectionInformation()?.let{ ci ->

                    val mainCapture = (application as MiApplication).getMainCapture(ar)
                    mainCapture.putListener(MainChannelObserver(ar.account))
                }

            }
        }
    }

    private inner class MainChannelObserver(
        val account: Account
    ) : MainCapture.AbsListener(){
        override fun notification(notification: Notification) {
            Handler(Looper.getMainLooper()).post{
                //val miApplication = applicationContext as MiApplication
                synchronized(mStopNotificationAccountMap){

                    if(mStopNotificationAccountMap[account.id] == null){
                        Log.d(TAG, "notification,:$notification")
                        showNotification(NotificationViewData(notification, account))
                    }else{
                        Log.d(TAG, "通知を表示しなかった")
                    }

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
                FOLLOW ->{
                    //builder.setSmallIcon(R.drawable.ic_follow)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.followed_by))

                }
                MENTION ->{
                    //builder.setSmallIcon(R.drawable.ic_mention)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.mention_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.text))

                }
                REPLY ->{
                    //builder.setSmallIcon(R.drawable.ic_reply_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.replied_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.text))
                }
                QUOTE ->{
                    //builder.setSmallIcon(R.drawable.ic_format_quote_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.quoted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.toShowNote?.text))
                }
                POLL_VOTE->{
                    //builder.setSmallIcon(R.drawable.ic_poll_black_24dp)
                    builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.voted_by))
                }
                REACTION ->{
                    //builder.setSmallIcon(R.drawable.ic_reaction_like)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.reacted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.reaction))
                }
                RENOTE ->{
                    // builder.setSmallIcon(R.drawable.ic_re_note)
                    builder.setContentTitle(notification.user.getDisplayUserName() + applicationContext.getString(R.string.renoted_by))
                    builder.setContentText(SafeUnbox.unbox(notification.noteViewData?.toShowNote?.text))
                }
                RECEIVE_FOLLOW_REQUEST ->{
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

        val pendingIntent = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(makeResultActivityIntent(notification))
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pendingIntent)

        mNotificationManager = with(makeNotificationManager(NOTIFICATION_CHANNEL_ID)){
            notify(5, builder.build())
            this
        }

    }

    private fun makeResultActivityIntent(notificationViewData: NotificationViewData): Intent{
        return when(notificationViewData.type){
            FOLLOW, RECEIVE_FOLLOW_REQUEST->{
                Intent(this, UserDetailActivity::class.java).apply{
                    putExtra(UserDetailActivity.EXTRA_USER_ID, notificationViewData.user.id)
                    putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            MENTION, REPLY, RENOTE, QUOTE, REACTION, POLL_VOTE ->{
                Intent(this, NoteDetailActivity::class.java).apply{
                    putExtra(NoteDetailActivity.EXTRA_NOTE_ID, notificationViewData.noteViewData?.id)
                    putExtra(NoteDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            else -> Intent(this, MainActivity::class.java)
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


    fun subscribeNotifications(){
        cancelAllNotification()
    }

    fun stopShowPushNotification(account: Account){
        synchronized(mStopNotificationAccountMap){
            mStopNotificationAccountMap[account.id] = account
            Log.d(TAG, "指定のアカウントのプッシュ通知を表示しない")
        }
    }

    fun startShowPushNotification(account: Account){
        synchronized(mStopNotificationAccountMap){
            mStopNotificationAccountMap.remove(account.id)
            Log.d(TAG, "プッシュ通知の表示を再開する")

        }
    }

    /*class ClientMessageHandler(service: NotificationService) : Handler(){

        private val mService: WeakReference<NotificationService> = WeakReference(service)

        fun getService(): NotificationService?{
            return mService.get()
        }

        // client（Activity）などからメッセージを受信したとき
        override fun handleMessage(msg: android.os.Message?) {
            super.handleMessage(msg)

            when(msg?.what){
                SUBSCRIBE_ALL_NOTIFICATIONS ->{
                    mService.get()?.cancelAllNotification()
                }
                STOP_PUSH_NOTIFICATION ->{
                    mService.get()?.isShowNotification = false
                }
                START_PUSH_NOTIFICATION->{
                    mService.get()?.isShowNotification = true
                }
            }

            //msg?.replyTo?.send()

        }
    }*/

    inner class NotificationBinder : Binder() {

        fun getService() = this@NotificationService
    }


}
