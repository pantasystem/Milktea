package jp.panta.misskeyandroidclient.ui.list.viewmodel

import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User

data class UserListEvent(
    val type: Type,
    val userListId: net.pantasystem.milktea.model.list.UserList.Id,

    //push user, pull user
    val userId: net.pantasystem.milktea.model.user.User.Id? = null,

    //create
    val userList: net.pantasystem.milktea.model.list.UserList? = null,

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