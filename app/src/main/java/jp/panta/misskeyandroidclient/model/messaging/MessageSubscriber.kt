package jp.panta.misskeyandroidclient.model.messaging

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.model.account.Account


class MessageSubscriber(val miCore: MiCore){

    private var beforeAccounts: List<Account>? = null
    private val accountMessagingObserverMap = HashMap<Long, UserMessagingObserver>()

    init{
        miCore.getAccounts().observeForever { accounts ->

            (beforeAccounts?: emptyList())
                .filter{ out ->
                    accounts.any { inner ->
                        out == inner
                    }
                }
                .forEach {  a ->
                    accountMessagingObserverMap.remove(a.accountId)
                }
            beforeAccounts = accounts


        }
    }

    fun getAllMergedAccountMessages(): Observable<Message>{
        val observables = (miCore.getAccounts().value?: emptyList()).map{
            getAccountMessageObservable(it)
        }
        return Observable.merge(observables)
    }

    fun getObservable(messagingId: MessagingId, ac: Account): Observable<Message>{
        synchronized(accountMessagingObserverMap){
            var observer = accountMessagingObserverMap[ac.accountId]
            if(observer == null){
                val mainCapture = miCore.getMainCapture(ac)
                observer = UserMessagingObserver(ac)
                accountMessagingObserverMap[ac.accountId] = observer
                mainCapture.putListener(observer)
            }
            return observer.getObservable(messagingId)
        }

    }

    fun getAccountMessageObservable(ac: Account): Observable<Message>{
        synchronized(accountMessagingObserverMap){
            var observer = accountMessagingObserverMap[ac.accountId]
            if(observer == null){
                val mainCapture = miCore.getMainCapture(ac)
                observer = UserMessagingObserver(ac)
                accountMessagingObserverMap[ac.accountId] = observer
                mainCapture.putListener(observer)
            }
            return observer.getObservable()
        }

    }

    fun getUnreadMessageStore(ac: Account): UnReadMessageStore{
        synchronized(accountMessagingObserverMap){
            var observer = accountMessagingObserverMap[ac.accountId]
            if(observer == null){
                observer = UserMessagingObserver(ac)
                accountMessagingObserverMap[ac.accountId] = observer
                miCore.getMainCapture(ac).putListener(observer)
            }
            return observer.unreadMessageStore
        }

    }

    // アカウント１に対して１になる
    class UserMessagingObserver(val account: Account) : MainCapture.AbsListener(){

        private val messageSubject = PublishSubject.create<Message>()

        val unreadMessageStore = UnReadMessageStore(account)

        override fun messagingMessage(message: Message) {
            super.messagingMessage(message)

            messageSubject.onNext(message)
        }

        override fun unreadMessagingMessage(message: Message) {
            super.unreadMessagingMessage(message)

            unreadMessageStore.addUnReadMessage(message)
        }

        fun getObservable(messagingId: MessagingId): Observable<Message>{
            return messageSubject.filter{
                it.messagingId(account) == messagingId
            }.share()
        }

        fun getObservable(): Observable<Message>{
            return messageSubject.share()
        }


    }



}