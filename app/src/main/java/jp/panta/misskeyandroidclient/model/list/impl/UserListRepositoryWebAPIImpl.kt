package jp.panta.misskeyandroidclient.model.list.impl

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.list.CreateList
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.list.UserListRepository
import jp.panta.misskeyandroidclient.model.users.User
import javax.inject.Inject

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
        return return res.body()!!.toEntity(account)
    }

    override suspend fun update(listId: UserList.Id, name: String): UserList {
        TODO("Not yet implemented")
    }

    override suspend fun appendUser(listId: UserList.Id, userId: User.Id): UserList {
        TODO("Not yet implemented")
    }

    override suspend fun removeUser(listId: UserList.Id, userId: User.Id): UserList {
        TODO("Not yet implemented")
    }

    override suspend fun delete(listId: UserList.Id) {
        TODO("Not yet implemented")
    }
}