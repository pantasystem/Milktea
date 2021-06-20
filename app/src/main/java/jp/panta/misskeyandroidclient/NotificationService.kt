package jp.panta.misskeyandroidclient

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.notification.*
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.view.notification.notificationMessageScope
import java.util.*
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData.Type.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NotificationService : Service() {
    companion object{
        private const val TAG = "NotificationService"
        private const val NOTIFICATION_CHANNEL_ID = "jp.panta.misskeyandroidclient.NotificationService.NOTIFICATION_CHANNEL_ID"
        private const val MESSAGE_CHANEL_ID = "jp.panta.misskeyandroidclient.NotificationService.MESSAGE_CHANEL_ID"

    }

    private lateinit var mBinder: NotificationBinder
    private var mNotificationManager: NotificationManager? = null


    private val mStopNotificationAccountMap = HashMap<Long, Account>()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startObserve()
        Log.d(TAG, "serviceを開始した")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
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
                (it.second as? ChannelBody.Main.Notification)?.let{ body ->
                    it.first to body
                }
            }.filterNotNull().onEach {
                val notification = miApplication.getGetters().notificationRelationGetter.get(it.first, it.second.body)
                showNotification(notification)
            }.launchIn(coroutineScope + Dispatchers.IO)


            miApplication.messageStreamFilter.getAllMergedAccountMessages().onEach {
                val msgRelation = miApplication.getGetters().messageRelationGetter.get(it)
                showMessageNotification(msgRelation)
            }.launchIn(coroutineScope + Dispatchers.IO)

        }


    }

    fun showNotification(account: Account, notification: NotificationRelation) {
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

    private fun showNotification(notification: NotificationRelation) {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground)

        notificationMessageScope {
            builder.setContentTitle(notification.getMessage())
        }
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        val pendingIntent = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(makeResultActivityIntent(notification.notification))
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
                UserDetailActivity.newInstance(this, userId = notification.userId).apply{
                    putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            is ReceiveFollowRequestNotification -> {
                UserDetailActivity.newInstance(this, userId = notification.userId).apply{

                    putExtra(UserDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            is HasNote -> {
                NoteDetailActivity.newIntent(this, notification.noteId).apply{
                    putExtra(NoteDetailActivity.EXTRA_IS_MAIN_ACTIVE, false)
                }
            }
            else -> Intent(this, MainActivity::class.java)
        }
    }

    private fun showMessageNotification(message: MessageRelation){

        val builder = NotificationCompat.Builder(this, MESSAGE_CHANEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(message.user.getDisplayUserName())
            .setContentText(SafeUnbox.unbox(message.message.text))
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
