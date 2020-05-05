package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.reactivex.subjects.BehaviorSubject
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NotificationSubscribeViewModel(val accountRelation: AccountRelation, miCore: MiCore) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val accountRelation: AccountRelation, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NotificationSubscribeViewModel(accountRelation, miCore) as T
        }
    }
    val unreadNotifications = MutableLiveData<List<Notification>>()

    private val mUnreadNotificationMap = HashMap<String, Notification>()

    private val notificationListener = Listener()
    init{
        miCore.getMainCapture(accountRelation).putListener(notificationListener)
    }

    val observeNotification = BehaviorSubject.create<Notification>()

    val currentNotification = MutableLiveData<Notification>()


    inner class Listener : MainCapture.AbsListener(){

        override fun notification(notification: Notification) {
            super.notification(notification)
            observeNotification.onNext(notification)
            currentNotification.postValue(notification)

            synchronized(mUnreadNotificationMap){
                mUnreadNotificationMap[notification.id] = notification
            }
        }

        override fun readAllNotifications() {
            super.readAllNotifications()

            synchronized(mUnreadNotificationMap){
                mUnreadNotificationMap.clear()
                unreadNotifications.postValue(mUnreadNotificationMap.values.toList())
            }
        }


    }



    fun subscribe(notification: Notification){
        synchronized(mUnreadNotificationMap){
            mUnreadNotificationMap.remove(notification.id)
        }
        viewModelScope.launch(Dispatchers.IO){
            val sorted= synchronized(mUnreadNotificationMap){
                mUnreadNotificationMap.values.sortedBy {
                    it.createdAt
                }
            }
            unreadNotifications.postValue(sorted)
        }
    }



}