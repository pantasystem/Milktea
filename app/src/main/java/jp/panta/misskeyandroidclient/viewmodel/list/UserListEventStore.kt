package jp.panta.misskeyandroidclient.viewmodel.list

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.list.UserListEvent.Type.*

class UserListEventStore (
    val misskeyAPI: MisskeyAPI,
    val accountRelation: AccountRelation
){

    companion object{
        private val eventSubject = PublishSubject.create<UserListEvent>()
    }

    fun onPullUser(userListId: String, userId: String){
        eventSubject.onNext(
            UserListEvent(
                type = PULL_USER,
                account = accountRelation.account,
                userListId = userListId,
                userId = userId
            )
        )
    }

    fun onPushUser(userListId: String, userId: String){
        eventSubject.onNext(
            UserListEvent(
                type = PUSH_USER,
                account = accountRelation.account,
                userListId = userListId,
                userId = userId
            )
        )
    }

    fun onCreateUserList(userList: UserList){
        eventSubject.onNext(
            UserListEvent(
                type = CREATE,
                account = accountRelation.account,
                userListId = userList.id,
                userList = userList,
                userId = null
            )
        )
    }

    fun onDeleteUserList(userList: UserList){
        eventSubject.onNext(
            UserListEvent(
                type = DELETE,
                account = accountRelation.account,
                userListId = userList.id,
                userList = userList
            )
        )
    }

    fun onUpdateUserList(listId: String, name: String){
        eventSubject.onNext(
            UserListEvent(
                type = UPDATED_NAME,
                account = accountRelation.account,
                userListId = listId,
                name = name
            )
        )
    }

    fun getEventStream(): Observable<UserListEvent>{
        return eventSubject.filter {
            it.account.id == accountRelation.account.id
        }
    }
}