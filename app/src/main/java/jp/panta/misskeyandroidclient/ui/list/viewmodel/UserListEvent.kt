package jp.panta.misskeyandroidclient.ui.list.viewmodel

import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User

data class UserListEvent(
    val type: Type,
    val userListId: UserList.Id,

    //push user, pull user
    val userId: User.Id? = null,

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