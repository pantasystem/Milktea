package jp.panta.misskeyandroidclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.ui.SafeUnbox
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.messaging.MessageRelationGetter
import net.pantasystem.milktea.model.messaging.MessageObserver
import net.pantasystem.milktea.model.messaging.MessageRelation
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotificationService : Service() {
    companion object {
        private const val TAG = "NotificationService"
        private const val MESSAGE_CHANNEL_ID =
            "jp.panta.misskeyandroidclient.NotificationService.MESSAGE_CHANNEL_ID"

    }

    private lateinit var mBinder: NotificationBinder

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    @Inject
    lateinit var messageObserver: MessageObserver

    @Inject
    lateinit var messageRelationGetter: MessageRelationGetter

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }


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
    private fun startObserve() {

        val miApplication = applicationContext
        if (miApplication is MiApplication) {


            messageObserver.observeAllAccountsMessages().onEach {
                val msgRelation = messageRelationGetter.get(it)
                showMessageNotification(msgRelation)
            }.launchIn(coroutineScope + Dispatchers.IO)

        }


    }


    private fun showMessageNotification(message: MessageRelation) {

        val builder = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(message.user.displayUserName)
            .setContentText(SafeUnbox.unbox(message.message.text))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        with(makeNotificationManager(MESSAGE_CHANNEL_ID)) {
            notify(6, builder.build())
        }
    }

    private fun makeNotificationManager(id: String): NotificationManager {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.app_name)
        val description = "THE NOTIFICATION"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(id) == null) {
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
                channel.description = description
                notificationManager.createNotificationChannel(channel)
            }
        }
        return notificationManager

    }


    inner class NotificationBinder : Binder()


}
