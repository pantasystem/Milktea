package jp.panta.misskeyandroidclient.viewmodel.list

import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User

data class UserListEvent(
    val type: Type,
    val account: Account,
    val userListId: String,

    //push user, pull user
    val user: User? = null,

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