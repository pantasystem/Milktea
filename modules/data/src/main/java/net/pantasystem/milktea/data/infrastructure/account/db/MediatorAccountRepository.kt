package net.pantasystem.milktea.data.infrastructure.account.db

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRepository

/**
 * データベースの内容をメモリにキャッシュしデータベースを制御する。
 * Writeは遅くなるがReadは高速化することが期待できる。
 */
class MediatorAccountRepository(
    private val roomAccountRepository: RoomAccountRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : AccountRepository {

    private var mAccountMap: Map<Long, Account> = mapOf()
    private var mAccounts: List<Account> = listOf()
        set(value) {
            field = value
            mAccountMap = value.associateBy {
                it.accountId
            }
        }


    override suspend fun add(account: Account, isUpdatePages: Boolean): Result<Account> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                roomAccountRepository.add(account, isUpdatePages).also {
                    mAccounts = roomAccountRepository.findAll().getOrThrow()
                }.getOrThrow()
            }
        }
    }

    override suspend fun delete(account: Account) {
        withContext(ioDispatcher) {
            roomAccountRepository.delete(account).also {
                mAccounts = roomAccountRepository.findAll().getOrThrow()
            }
        }
    }

    override suspend fun findAll(): Result<List<Account>> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                if(mAccounts.isEmpty()) {
                    mAccounts = roomAccountRepository.findAll().getOrThrow()
                }
                mAccounts
            }
        }
    }


    override suspend fun get(accountId: Long): Result<Account> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val inMem = mAccountMap[accountId]
                if (inMem != null) {
                    return@withContext inMem
                }
                findAll().getOrThrow().firstOrNull {
                    it.accountId == accountId
                }?: throw AccountNotFoundException(accountId)
            }
        }

    }

    override suspend fun getCurrentAccount(): Result<Account> {
        return withContext(ioDispatcher) {
            roomAccountRepository.getCurrentAccount()
        }
    }

    override suspend fun setCurrentAccount(account: Account): Result<Account> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
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