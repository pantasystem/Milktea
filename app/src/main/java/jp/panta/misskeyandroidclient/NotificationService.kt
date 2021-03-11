package jp.panta.misskeyandroidclient

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.GsonBuilder
import io.reactivex.disposables.CompositeDisposable
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.notification.*
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.view.SafeUnbox
import java.util.*
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData.Type.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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

    private val mStopNotificationAccountMap = HashMap<Long, Account>()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private val mDisposable = CompositeDisposable()

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

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun startObserve(){

        val miApplication = applicationContext
        if(miApplication is MiApplication){

            miApplication.getAccounts().flatMapLatest { acList ->
                acList.map{ a ->
                    miApplication.getChannelAPI(a).connect(ChannelAPI.Type.MAIN).map {
                        a to it
                    }
                }.merge()
            }.filterNot {
                mStopNotificationAccountMap.contains(it.first.accountId)
            }.map {
                it.second as? ChannelBody.Main.Notification
            }.filterNotNull().onEach {
                showNotification(it.body)
            }.launchIn(coroutineScope + Dispatchers.IO)


            miApplication.messageStreamFilter.getAllMergedAccountMessages().onEach {
                showMessageNotification(it)
            }.launchIn(coroutineScope)

        }


    }

    fun showNotification(account: Account, notification: Notification) {
        Handler(Looper.getMainLooper()).post{
            //val miApplication = applicationContext as MiApplication
            synchronized(mStopNotificationAccountMap){

                if(mStopNotificationAccountMap[account.accountId] == null){
                    Log.d(TAG, "notification,:$notification")
                    showNotification(notification)
                }else{
                    Log.d(TAG, "通知を表示しなかった")
                }

            }

        }
    }

    private fun showNotification(notification: Notification) {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground)

        when(notification) {
            is FollowNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName() + " " +applicationContext.getString(R.string.followed_by))
            }
            is MentionNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName()  + " " + applicationContext.getString(R.string.mention_by))
                builder.setContentText(SafeUnbox.unbox(notification.note.text))
            }
            is ReplyNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName()  + " " + getString(R.string.replied_by))
                builder.setContentText(SafeUnbox.unbox(notification.note.text))
            }
            is QuoteNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName()  + " " + getString(R.string.quoted_by))
                builder.setContentText(SafeUnbox.unbox(notification.note.text))
            }
            is PollVoteNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName()  + " " + getString(R.string.voted_by))
            }
            is ReactionNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName()  + " " + applicationContext.getString(R.string.reacted_by))
                builder.setContentText(SafeUnbox.unbox(notification.reaction))
            }
            is RenoteNotification -> {
                // builder.setSmallIcon(R.drawable.ic_re_note)
                builder.setContentTitle(notification.user.getDisplayUserName()  + " " + applicationContext.getString(R.string.renoted_by))
                builder.setContentText(SafeUnbox.unbox(notification.note.reNote?.text?: ""))
            }
            is ReceiveFollowRequestNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName() + getString(R.string.request_follow))
            }
            is FollowRequestAcceptedNotification -> {
                builder.setContentTitle(notification.user.getDisplayUserName() + " " + getString(R.string.follow_request_accepted))
            }



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

    private fun makeResultActivityIntent(notification: Notification): Intent{
        return when(notification){
            is FollowNotification -> {
                Intent(this, UserDetailActivity::class.java).apply{
                    putExtra(UserDetailActivity.EXTRA_USER_ID, notification.user.id)
                    putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            is ReceiveFollowRequestNotification -> {
                Intent(this, UserDetailActivity::class.java).apply{
                    putExtra(UserDetailActivity.EXTRA_USER_ID, notification.user.id)
                    putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            is HasNote -> {
                Intent(this, NoteDetailActivity::class.java).apply{
                    putExtra(NoteDetailActivity.EXTRA_NOTE_ID, notification.note.id)
                    putExtra(NoteDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            else -> Intent(this, MainActivity::class.java)
        }
    }

    private fun showMessageNotification(message: MessageDTO){

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
            mStopNotificationAccountMap[account.accountId] = account
            Log.d(TAG, "指定のアカウントのプッシュ通知を表示しない")
        }
    }

    fun startShowPushNotification(account: Account){
        synchronized(mStopNotificationAccountMap){
            mStopNotificationAccountMap.remove(account.accountId)
            Log.d(TAG, "プッシュ通知の表示を再開する")

        }
    }


    inner class NotificationBinder : Binder() {

        fun getService() = this@NotificationService
    }


}
