package jp.panta.misskeyandroidclient.viewmodel.list

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.list.UserList

data class UserListEvent(
    val type: Type,
    val account: Account,
    val userListId: String,

    //push user, pull user
    val userId: String? = null,

    //create
    val userList: UserList? = null,

    val name: String? = null
) {
    enum class Type{
        PUSH_USER,
        PULL_USER,
        UPDATED_NAME,
        DELETE,
        CREATE
    }
}