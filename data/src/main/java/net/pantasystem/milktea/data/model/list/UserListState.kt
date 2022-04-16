package net.pantasystem.milktea.data.model.list

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
            },
        ).appendAll(userList.id.accountId, listOf(userList))
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
                map[accountId] = list.distinct()
            }
        )
    }

    fun replaceAll(accountId: Long, lists: List<UserList>): UserListState {
        return copy(
            userListsMap = userListsMap.toMutableMap().also { map ->
                map.putAll(lists.map {
                    it.id to it
                })
            },
            pagedIdsAccountMap = pagedIdsAccountMap.toMutableMap().also { map ->
                map[accountId] = lists.map {
                    it.id
                }
            }

        )
    }

    fun prependAll(accountId: Long, lists: List<UserList>): UserListState {
        return copy(
            userListsMap = userListsMap.toMutableMap().also { map ->
                map.putAll(lists.map { it.id to it })
            },
            pagedIdsAccountMap = pagedIdsAccountMap.toMutableMap().also { map ->
                val list = map[accountId]?.toMutableList() ?: mutableListOf()
                list.addAll(0, lists.map {
                    it.id
                })
                map[accountId] = list.distinct()
            }
        )
    }

    fun getUserLists(accountId: Long): List<UserList> {
        val page = pagedIdsAccountMap[accountId]?: emptyList()
        return page.mapNotNull {
            userListsMap[it]
        }
    }

    fun get(id: UserList.Id): UserList? {
        return userListsMap[id]
    }
}