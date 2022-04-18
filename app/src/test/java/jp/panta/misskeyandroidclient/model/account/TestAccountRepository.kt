package jp.panta.misskeyandroidclient.model.account

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRepository

class TestAccountRepository : net.pantasystem.milktea.model.account.AccountRepository {

    val accounts = mutableMapOf(
        1L to net.pantasystem.milktea.model.account.Account(
            "remote1",
            "test.misskey.jp",
            "test1",
            "token",
            emptyList(),
            net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
            1
        ),
        2L to net.pantasystem.milktea.model.account.Account(
            "remote2",
            "test.misskey.jp",
            "test2",
            "token",
            emptyList(),
            net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
            2
        ),
        3L to net.pantasystem.milktea.model.account.Account(
            "remote3",
            "test.misskey.jp",
            "test1",
            "token",
            emptyList(),
            net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
            3
        )
    )

    private var currentAccountId = 1L

    override suspend fun add(account: net.pantasystem.milktea.model.account.Account, isUpdatePages: Boolean): net.pantasystem.milktea.model.account.Account {
        val ac = account.copy(
            accountId = accounts.size.toLong()
        )
        accounts[accounts.size.toLong()] = ac
        return ac
    }

    override fun addEventListener(listener: net.pantasystem.milktea.model.account.AccountRepository.Listener) {

    }

    override fun removeEventListener(listener: net.pantasystem.milktea.model.account.AccountRepository.Listener) {

    }

    override suspend fun delete(account: net.pantasystem.milktea.model.account.Account) {
        accounts.remove(account.accountId)
    }

    override suspend fun findAll(): List<net.pantasystem.milktea.model.account.Account> {
        return accounts.values.toList()
    }

    override suspend fun get(accountId: Long): net.pantasystem.milktea.model.account.Account {
        return accounts[accountId]?: throw net.pantasystem.milktea.model.account.AccountNotFoundException()
    }

    override suspend fun getCurrentAccount(): net.pantasystem.milktea.model.account.Account {
        return get(currentAccountId)
    }

    override suspend fun setCurrentAccount(account: net.pantasystem.milktea.model.account.Account): net.pantasystem.milktea.model.account.Account {
        currentAccountId = account.accountId
        return account
    }
}