package net.pantasystem.milktea.data.infrastructure.account.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    override suspend fun add(account: Account, isUpdatePages: Boolean): Result<Account> {
        return runCatching {
            withContext(Dispatchers.IO) {
                roomAccountRepository.add(account, isUpdatePages).also {
                    mAccounts = roomAccountRepository.findAll().getOrThrow()
                }.getOrThrow()
            }
        }
    }

    override suspend fun delete(account: Account) {
        withContext(Dispatchers.IO) {
            roomAccountRepository.delete(account).also {
                mAccounts = roomAccountRepository.findAll().getOrThrow()
            }
        }
    }

    override suspend fun findAll(): Result<List<Account>> {
        return runCatching {
            withContext(Dispatchers.IO) {
                if(mAccounts.isEmpty()) {
                    mAccounts = roomAccountRepository.findAll().getOrThrow()
                }
                mAccounts
            }
        }
    }


    override suspend fun get(accountId: Long): Result<Account> {
        return runCatching {
            withContext(Dispatchers.IO) {
                findAll().getOrThrow().firstOrNull {
                    it.accountId == accountId
                }?: throw AccountNotFoundException(accountId)
            }
        }

    }

    override suspend fun getCurrentAccount(): Result<Account> {
        return withContext(Dispatchers.IO) {
            roomAccountRepository.getCurrentAccount()
        }
    }

    override suspend fun setCurrentAccount(account: Account): Result<Account> {
        return runCatching {
            withContext(Dispatchers.IO) {
                roomAccountRepository.setCurrentAccount(account).getOrThrow().also {
                    mAccounts = findAll().getOrThrow()
                }
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