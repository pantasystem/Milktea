package net.pantasystem.milktea.data.model.account.db

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRepository

/**
 * データベースの内容をメモリにキャッシュしデータベースを制御する。
 * Writeは遅くなるがReadは高速化することが期待できる。
 */
class MediatorAccountRepository(
    private val roomAccountRepository: RoomAccountRepository
) : AccountRepository {

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


    override suspend fun get(accountId: Long): Account {
        return findAll().firstOrNull {
            it.accountId == accountId
        }?: throw AccountNotFoundException()
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