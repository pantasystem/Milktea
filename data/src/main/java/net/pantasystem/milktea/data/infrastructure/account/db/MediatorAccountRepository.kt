package net.pantasystem.milktea.data.infrastructure.account.db

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
            mAccounts = roomAccountRepository.findAll().getOrThrow()
        }
    }

    override suspend fun delete(account: Account) {
        return roomAccountRepository.delete(account).also {
            mAccounts = roomAccountRepository.findAll().getOrThrow()
        }
    }

    override suspend fun findAll(): Result<List<Account>> {
        return runCatching {
            if(mAccounts.isEmpty()) {
                mAccounts = roomAccountRepository.findAll().getOrThrow()
            }
            mAccounts
        }
    }


    override suspend fun get(accountId: Long): Result<Account> {
        return runCatching {
            findAll().getOrThrow().firstOrNull {
                it.accountId == accountId
            }?: throw AccountNotFoundException(accountId)
        }

    }

    override suspend fun getCurrentAccount(): Result<Account> {
        return roomAccountRepository.getCurrentAccount()
    }

    override suspend fun setCurrentAccount(account: Account): Result<Account> {
        return runCatching {
            roomAccountRepository.setCurrentAccount(account).getOrThrow().also {
                mAccounts = findAll().getOrThrow()
            }
        }
    }

    override fun addEventListener(listener: AccountRepository.Listener) {
        roomAccountRepository.addEventListener(listener)
    }

    override fun removeEventListener(listener: AccountRepository.Listener) {
        roomAccountRepository.removeEventListener(listener)
    }
}