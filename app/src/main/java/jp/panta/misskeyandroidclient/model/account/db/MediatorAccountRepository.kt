package jp.panta.misskeyandroidclient.model.account.db

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository

/**
 * データベースの内容をメモリにキャッシュしデータベースを制御する。
 * Writeは遅くなるがReadは高速化することが期待できる。
 */
class MediatorAccountRepository(
    private val roomAccountRepository: RoomAccountRepository
) : AccountRepository{

    private var mAccounts: List<Account> = listOf()

    override suspend fun add(account: Account, isUpdatePages: Boolean): Account {
        return roomAccountRepository.add(account, isUpdatePages).also {
            mAccounts = roomAccountRepository.findAll()
        }
    }

    override suspend fun delete(account: Account) {
        return roomAccountRepository.delete(account).also {
            mAccounts = roomAccountRepository.findAll()
        }
    }

    override suspend fun findAll(): List<Account> {
        if(mAccounts.isEmpty()) {
            mAccounts = roomAccountRepository.findAll()
        }
        return mAccounts
    }

    override suspend fun findAllByUserName(userName: String): List<Account> {
        return findAll().filter {
            it.userName == userName
        }
    }

    override suspend fun get(accountId: Long): Account {
        return findAll().firstOrNull {
            it.accountId == accountId
        }?: throw AccountNotFoundException()
    }

    override suspend fun findByRemoteIdAndInstanceDomain(
        remoteId: String,
        instanceDomain: String
    ): Account? {
        return findAll().firstOrNull {
            it.remoteId == remoteId && it.instanceDomain == instanceDomain
        }
    }

    override suspend fun findByUserNameAndInstanceDomain(
        userName: String,
        instanceDomain: String
    ): Account? {
        return findAll().firstOrNull {
            it.userName == userName && it.instanceDomain == instanceDomain
        }
    }

    override suspend fun getCurrentAccount(): Account {
        return roomAccountRepository.getCurrentAccount()
    }

    override suspend fun setCurrentAccount(account: Account): Account {
        return roomAccountRepository.setCurrentAccount(account).also {
            mAccounts = findAll()
        }
    }

    override fun addEventListener(listener: AccountRepository.Listener) {
        roomAccountRepository.addEventListener(listener)
    }

    override fun removeEventListener(listener: AccountRepository.Listener) {
        roomAccountRepository.removeEventListener(listener)
    }
}