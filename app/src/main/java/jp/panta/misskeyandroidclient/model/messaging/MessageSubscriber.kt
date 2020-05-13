package jp.panta.misskeyandroidclient.model.messaging

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.viewmodel.MiCore


class MessageSubscriber(val miCore: MiCore){

    private var beforeAccounts: List<Account>? = null
    private val accountMessagingObserverMap = HashMap<Account, UserMessagingObserver>()

    init{
        miCore.accounts.observeForever {
            val accounts = it.map{ ar ->
                ar.account
            }

            (beforeAccounts?: emptyList())
                .filter{ out ->
                    accounts.any { inner ->
                        out == inner
                    }
                }
                .forEach {  a ->
                    accountMessagingObserverMap.remove(a)
                }
            beforeAccounts = accounts


        }
    }

    fun getAllMergedAccountMessages(): Observable<Message>{
        val observables = (miCore.accounts.value?: emptyList()).map{
            getAccountMessageObservable(it)
        }
        return Observable.merge(observables)
    }

    fun getObservable(messagingId: MessagingId, ar: AccountRelation): Observable<Message>{
        synchronized(accountMessagingObserverMap){
            var observer = accountMessagingObserverMap[ar.account]
            if(observer == null){
                miCore.getMainCapture(ar)
                observer = UserMessagingObserver(ar.account)
            }
            return observer.getObservable(messagingId)
        }

    }

    fun getAccountMessageObservable(ar: AccountRelation): Observable<Message>{
        synchronized(accountMessagingObserverMap){
            var observer = accountMessagingObserverMap[ar.account]
            if(observer == null){
                miCore.getMainCapture(ar)
                observer = UserMessagingObserver(ar.account)
            }
            return observer.getObservable()
        }

    }

    fun getUnreadMessageStore(ar: AccountRelation): UnReadMessageStore{
        synchronized(accountMessagingObserverMap){
            var observer = accountMessagingObserverMap[ar.account]
            if(observer == null){
                miCore.getMainCapture(ar)
                observer = UserMessagingObserver(ar.account)
            }
            return observer.unreadMessageStore
        }

    }

    // アカウント１に対して１になる
    inner class UserMessagingObserver(val account: Account) : MainCapture.AbsListener(){

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