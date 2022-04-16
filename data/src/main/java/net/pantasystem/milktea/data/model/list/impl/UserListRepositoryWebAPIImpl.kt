package net.pantasystem.milktea.data.model.list.impl

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.api.misskey.list.CreateList
import net.pantasystem.milktea.data.api.misskey.list.ListId
import net.pantasystem.milktea.data.api.misskey.list.ListUserOperation
import net.pantasystem.milktea.data.api.misskey.list.UpdateList
import net.pantasystem.milktea.data.api.misskey.throwIfHasError
import net.pantasystem.milktea.data.model.Encryption
import net.pantasystem.milktea.data.model.I
import net.pantasystem.milktea.data.model.account.AccountRepository
import net.pantasystem.milktea.data.model.list.UserList
import net.pantasystem.milktea.data.model.list.UserListRepository
import net.pantasystem.milktea.data.model.users.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserListRepositoryWebAPIImpl @Inject constructor(
    val encryption: Encryption,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val accountRepository: AccountRepository
) : UserListRepository {
    override suspend fun findByAccountId(accountId: Long): List<UserList> {
        val account = accountRepository.get(accountId)
        val api = misskeyAPIProvider.get(account)
        val body = api.userList(I(account.getI(encryption)))
            .throwIfHasError()
            .body()
        return body!!.map {
            it.toEntity(account)
        }
    }

    override suspend fun create(accountId: Long, name: String): UserList {
        val account = accountRepository.get(accountId)
        val res = misskeyAPIProvider.get(account).createList(
            CreateList(
                account.getI(encryption),
                name = name
            )
        ).throwIfHasError()
        return res.body()!!.toEntity(account)
    }

    override suspend fun update(listId: UserList.Id, name: String) {
        val account = accountRepository.get(listId.accountId)
        misskeyAPIProvider.get(account).updateList(
            UpdateList(
                account.getI(encryption),
                name = name,
                listId = listId.userListId
            )
        ).throwIfHasError()
    }

    override suspend fun appendUser(listId: UserList.Id, userId: User.Id) {
        val account = accountRepository.get(listId.accountId)
        val misskeyAPI = misskeyAPIProvider.get(account)
        misskeyAPI.pushUserToList(ListUserOperation(
            userId = userId.id,
            listId = listId.userListId,
            i = account.getI(encryption)
        )).throwIfHasError()
    }

    override suspend fun removeUser(listId: UserList.Id, userId: User.Id) {
        val account = accountRepository.get(listId.accountId)
        val misskeyAPI = misskeyAPIProvider.get(account)
        misskeyAPI.pullUserFromList(ListUserOperation(
            userId = userId.id,
            listId = listId.userListId,
            i = account.getI(encryption)
        )).throwIfHasError()
    }

    override suspend fun delete(listId: UserList.Id) {
        val account = accountRepository.get(listId.accountId)
        val misskeyAPI = misskeyAPIProvider.get(account)
        misskeyAPI.deleteList(ListId(account.getI(encryption), listId.userListId))
            .throwIfHasError()
    }

    override suspend fun findOne(userListId: UserList.Id): UserList {
        val account = accountRepository.get(userListId.accountId)
        val misskeyAPI = misskeyAPIProvider.get(account)
        val res = misskeyAPI.showList(ListId(account.getI(encryption), userListId.userListId))
            .throwIfHasError()
        return res.body()!!.toEntity(account)
    }
}