package net.pantasystem.milktea.model.list

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.user.User

interface UserListRepository {
    suspend fun findByAccountId(accountId: Long): List<UserList>

    suspend fun create(accountId: Long, name: String): UserList

    suspend fun update(listId: UserList.Id, name: String)

    suspend fun appendUser(listId: UserList.Id, userId: User.Id)

    suspend fun removeUser(listId: UserList.Id, userId: User.Id)

    suspend fun delete(listId: UserList.Id)

    suspend fun findOne(userListId: UserList.Id): UserList

    suspend fun syncByAccountId(accountId: Long): Result<Unit>

    suspend fun syncOne(userListId: UserList.Id): Result<Unit>

    fun observeByAccountId(accountId: Long): Flow<List<UserListWithMembers>>

    fun observeOne(userListId: UserList.Id): Flow<UserListWithMembers?>
}