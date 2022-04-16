package net.pantasystem.milktea.data.model.list

import net.pantasystem.milktea.data.model.users.User

interface UserListRepository {
    suspend fun findByAccountId(accountId: Long): List<UserList>

    suspend fun create(accountId: Long, name: String): UserList

    suspend fun update(listId: UserList.Id, name: String)

    suspend fun appendUser(listId: UserList.Id, userId: User.Id)

    suspend fun removeUser(listId: UserList.Id, userId: User.Id)

    suspend fun delete(listId: UserList.Id)

    suspend fun findOne(userListId: UserList.Id): UserList
}