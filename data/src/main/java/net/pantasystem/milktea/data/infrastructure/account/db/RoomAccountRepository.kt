package net.pantasystem.milktea.data.infrastructure.account.db

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.account.page.db.PageDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRegistrationFailedException
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import java.util.concurrent.Callable

const val CURRENT_ACCOUNT_ID_KEY = "CURRENT_ACCOUNT_ID"

class RoomAccountRepository(
    private val roomDataBase: DataBase,
    private val sharedPreferences: SharedPreferences,
    private val accountDao: AccountDAO,
    private val pageDAO: PageDAO,
) : AccountRepository {

    private val listeners = mutableSetOf<AccountRepository.Listener>()

    override fun addEventListener(listener: AccountRepository.Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun removeEventListener(listener: AccountRepository.Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    override suspend fun add(account: Account, isUpdatePages: Boolean): Account {
        return roomDataBase.runInTransaction(Callable<Account> {
            var exAccount: Account? = null
            var isNeedDeepUpdate = isUpdatePages

            if (account.accountId > 0) {
                exAccount = accountDao.getAccountRelation(account.accountId)?.toAccount()
            }
            if (exAccount == null) {
                exAccount = accountDao.findByUserNameAndInstanceDomain(
                    account.userName,
                    account.instanceDomain
                )?.toAccount()
            }

            if (exAccount == null) {
                exAccount = accountDao.findByRemoteIdAndInstanceDomain(
                    account.remoteId,
                    account.instanceDomain
                )?.toAccount()
            }

            if (exAccount == null) {
                val id = accountDao.insert(account)
                exAccount = accountDao.get(id) ?: throw AccountRegistrationFailedException()
                Log.d("RoomAccountRepository", "insertしました: $exAccount")
                isNeedDeepUpdate = true
            } else {
                exAccount = exAccount.copy(
                    remoteId = account.remoteId,
                    instanceDomain = account.instanceDomain,
                    encryptedToken = account.encryptedToken,
                    userName = account.userName
                ).also {
                    accountDao.update(it)
                }
            }


            if (isNeedDeepUpdate) {
                val exPages = exAccount.pages
                val pages = account.pages.mapIndexed { i, page ->
                    page.also {
                        it.accountId = exAccount!!.accountId
                        it.weight = i
                    }
                }

                val pageMap = account.pages.associateBy {
                    it.pageId
                }

                val exPageMap = exAccount.pages.associateBy {
                    it.pageId
                }

                val addedPages = ArrayList<Page>()
                val updatedPages = ArrayList<Page>()

                for (page in pages) {
                    when {
                        page.pageId == 0L -> {
                            addedPages.add(page)
                        }
                        page != exPageMap[page.pageId] -> {
                            updatedPages.add(page)
                        }

                    }
                }

                val removedPages = exPages.filter {
                    pageMap[it.pageId] == null
                }
                Log.d("Repo", "削除されたページ:$removedPages ${exPages.size}, ${pages.size}")

                pageDAO.deleteAll(removedPages)
                pageDAO.updateAll(updatedPages)
                pageDAO.insertAll(addedPages)

                exAccount = runBlocking {
                    get(exAccount!!.accountId)
                }
                Log.d("Repo", "ex: $exAccount")
                publish(AccountRepository.Event.Created(account))
            } else {
                publish(AccountRepository.Event.Updated(account))
            }

            exAccount
        })

    }

    override suspend fun delete(account: Account) {
        accountDao.delete(account)
        publish(AccountRepository.Event.Deleted(account.accountId))
    }


    @Throws(AccountNotFoundException::class)
    override suspend fun get(accountId: Long): Account {
        return accountDao.getAccountRelation(accountId)?.toAccount()
            ?: throw AccountNotFoundException(
                accountId
            )
    }

    override suspend fun findAll(): List<Account> {
        return accountDao.findAll().map {
            it.toAccount()
        }
    }

    @Throws(AccountNotFoundException::class)
    override suspend fun getCurrentAccount(): Account {
        val currentAccountId = sharedPreferences.getLong(CURRENT_ACCOUNT_ID_KEY, -1)
        val current = accountDao.getAccountRelation(currentAccountId)
        return if (current == null) {
            val first = accountDao.findAll().firstOrNull()?.toAccount()
                ?: throw AccountNotFoundException(currentAccountId)
            setCurrentAccount(first)
        } else {
            current.toAccount()
        }

    }


    override suspend fun setCurrentAccount(account: Account): Account {
        val current = accountDao.get(account.accountId)
        val ac = if (current == null) {
            add(account)
        } else {
            account
        }
        sharedPreferences.edit().also {
            it.putLong(CURRENT_ACCOUNT_ID_KEY, ac.accountId)
        }.apply()
        publish(AccountRepository.Event.Updated(ac))

        return ac
    }


    private fun publish(e: AccountRepository.Event) {
        synchronized(listeners) {
            listeners.forEach {
                it.on(e)
            }
        }
    }


}