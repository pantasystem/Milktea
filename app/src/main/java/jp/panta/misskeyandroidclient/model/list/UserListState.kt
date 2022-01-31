package jp.panta.misskeyandroidclient.model.list

data class UserListState(
    val userListsMap: Map<UserList.Id, UserList>,
    val pagedIdsAccountMap: Map<Long, List<UserList.Id>> = emptyMap()
) {

    val userLists: List<UserList> = userListsMap.values.toList()

    fun deleted(userListId: UserList.Id): UserListState {
        return copy(
            userListsMap = userListsMap.toMutableMap().also {
                it.remove(userListId)
            }
        )
    }

    fun created(userList: UserList): UserListState {
        return copy(
            userListsMap = userListsMap.toMutableMap().also { map ->
                map[userList.id] = userList
            }
        )
    }

    fun updated(userList: UserList): UserListState {
        return copy(
            userListsMap = userListsMap.toMutableMap().also { map ->
                map[userList.id] = userList
            }
        )

    }

    fun appendAll(accountId: Long, lists: List<UserList>): UserListState {
        return copy(
            userListsMap = userListsMap.toMutableMap().also { map ->
                map.putAll(lists.map { it.id to it })
            },
            pagedIdsAccountMap = pagedIdsAccountMap.toMutableMap().also { map ->
                val list = map[accountId]?.toMutableList() ?: mutableListOf()
                list.addAll(lists.map {
                    it.id
                })
                map[accountId] = list
            }
        )
    }

    fun prependAll(accountId: Long, lists: List<UserList>): UserListState {
        return this
    }
}