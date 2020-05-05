package jp.panta.misskeyandroidclient.viewmodel.notification

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.subjects.BehaviorSubject
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
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
    class Factory(val accountRelation: AccountRelation, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NotificationSubscribeViewModel(miCore) as T
        }
    }
    private val accounts = miCore.accounts


    private val notificationListenerAccountMap = HashMap<Account, Listener>()


    init{
        accounts.observeForever { arList->
            arList.forEach{ ar ->
                val mainCapture = miCore.getMainCapture(ar)
                var listener = notificationListenerAccountMap[ar.account]
                if(listener == null){
                    listener = Listener(ar)
                    notificationListenerAccountMap[ar.account] = listener
                    mainCapture.putListener(listener)
                }
            }
        }
    }

    val observeNotification = BehaviorSubject.create<Notification>()

    fun getNotifications(account: AccountRelation): LiveData<List<Notification>>?{
        var listener = notificationListenerAccountMap[account.account]
        if(listener == null){
            listener = Listener(account)
            miCore.getMainCapture(account)
                .putListener(listener)
        }

        return listener.notificationsLiveData
    }


    inner class Listener(val account: AccountRelation) : MainCapture.AbsListener(){


        private val mUnReadNotifications = TreeSet<Notification>(kotlin.Comparator { o1, o2 ->
            o2.createdAt.compareTo(o1.createdAt)
        })
        val notificationsLiveData = MutableLiveData<List<Notification>>()



        override fun notification(notification: Notification) {
            super.notification(notification)

            mUnReadNotifications.add(notification)

            observeNotification.onNext(notification)
            updateLiveData()
            Log.d("NotificationSubscribeVM", "notification:$notification")
            Log.d("NotificationSubscribeVM", "notifications:${notificationsLiveData.value}")

        }

        override fun readAllNotifications() {
            super.readAllNotifications()

            mUnReadNotifications.clear()
            updateLiveData()

        }



        private fun updateLiveData(){
            notificationsLiveData.postValue(
                ArrayList(mUnReadNotifications.toList())
            )
        }

    }

}