package jp.panta.misskeyandroidclient.viewmodel.notification

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.subjects.BehaviorSubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import java.util.*
import kotlin.collections.HashMap

/**
 * アカウントごとの通知と通知の購読を管理する
 */
class NotificationSubscribeViewModel(private val miCore: MiCore) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val account: Account, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NotificationSubscribeViewModel(miCore) as T
        }
    }
    private val accounts = miCore.getAccounts()


    private val notificationListenerAccountMap = HashMap<Long, Listener>()

    val notifications = MediatorLiveData<List<Notification>?>().apply{

        addSource(miCore.getCurrentAccount()){
            value = getNotifications(it)?.value
        }
    }


    val observeNotification = BehaviorSubject.create<Notification>()

    fun getNotifications(account: Account): LiveData<List<Notification>?>?{
        var listener = notificationListenerAccountMap[account.accountId]
        if(listener == null){
            listener = Listener(account)
            miCore.getMainCapture(account)
                .putListener(listener)
            notificationListenerAccountMap[account.accountId] = listener
        }

        return listener.notificationsLiveData
    }

    fun readAllNotifications(account: Account? = miCore.getCurrentAccount().value){
        Log.d("NotificationSubscribeVM", "既読を開始した")
        account?: return
        Log.d("NotificationSubscribeVM", "既読試み中")

        val listener = notificationListenerAccountMap[account.accountId]
            ?: return

        listener.readAllNotification()
        Log.d("NotificationSubscribeVM", "既読完了")

    }


    inner class Listener(val account: Account) : MainCapture.AbsListener(){


        private val mUnReadNotifications = TreeSet<Notification> { o1, o2 ->
            o2.createdAt.compareTo(o1.createdAt)
        }
        val notificationsLiveData = MutableLiveData<List<Notification>?>()



        override fun notification(notification: Notification) {
            super.notification(notification)

            mUnReadNotifications.add(notification)

            observeNotification.onNext(notification)
            updateLiveData()
            Log.d("NotificationSubscribeVM", "notification:$notification")
            Log.d("NotificationSubscribeVM", "notifications:${notificationsLiveData.value}")

        }


        fun readAllNotification(){
            mUnReadNotifications.clear()
            updateLiveData()
        }

        private fun updateLiveData(){
            val data = if(mUnReadNotifications.isEmpty()){
                null
            }else{
                ArrayList(mUnReadNotifications.toList())
            }
            notificationsLiveData.postValue(data)
            if(miCore.getCurrentAccount().value?.accountId == account.accountId){
                notifications.postValue(data)
            }

            Log.d("NotificationSubscribeVM", "未読数:${mUnReadNotifications.size}")

        }


    }

}