package net.pantasystem.milktea.data.model.account.db

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.model.DataBase
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountNotFoundException
import net.pantasystem.milktea.model.account.AccountRegistrationFailedException
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.data.model.account.page.db.PageDAO
import java.util.concurrent.Callable

const val CURRENT_ACCOUNT_ID_KEY = "CURRENT_ACCOUNT_ID"
class RoomAccountRepository(
    private val roomDataBase: DataBase,
    private val sharedPreferences: SharedPreferences,
    private val accountDao: AccountDAO,
    private val pageDAO: PageDAO,
) : net.pantasystem.milktea.model.account.AccountRepository {

    private val listeners = mutableSetOf<net.pantasystem.milktea.model.account.AccountRepository.Listener>()

    override fun addEventListener(listener: net.pantasystem.milktea.model.account.AccountRepository.Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun removeEventListener(listener: net.pantasystem.milktea.model.account.AccountRepository.Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    override suspend fun add(account: net.pantasystem.milktea.model.account.Account, isUpdatePages: Boolean): net.pantasystem.milktea.model.account.Account {
        return roomDataBase.runInTransaction(Callable<net.pantasystem.milktea.model.account.Account> {
            var exAccount: net.pantasystem.milktea.model.account.Account? = null
            var isNeedDeepUpdate = isUpdatePages

            if(account.accountId > 0){
                exAccount = accountDao.getAccountRelation(account.accountId)?.toAccount()
            }
            if(exAccount == null){
                exAccount = accountDao.findByUserNameAndInstanceDomain(account.userName, account.instanceDomain)?.toAccount()
            }

            if(exAccount == null){
                exAccount = accountDao.findByRemoteIdAndInstanceDomain(account.remoteId, account.instanceDomain)?.toAccount()
            }

            if(exAccount == null){
                val id = accountDao.insert(account)
                exAccount = accountDao.get(id)?: throw net.pantasystem.milktea.model.account.AccountRegistrationFailedException()
                Log.d("RoomAccountRepository", "insertしました: $exAccount")
                isNeedDeepUpdate = true
            }else{
                exAccount = exAccount.copy(remoteId = account.remoteId, instanceDomain = account.instanceDomain, encryptedToken = account.encryptedToken, userName = account.userName).also {
                    accountDao.update(it)
                }
            }


            if(isNeedDeepUpdate){
                val exPages = exAccount.pages
                val pages = account.pages.mapIndexed{ i, page ->
                    page.also{
                        it.accountId = exAccount!!.accountId
                        it.weight = i
                    }
                }

                val pageMap = account.pages.map{
                    it.pageId to it
                }.toMap()

                val exPageMap = exAccount.pages.map{
                    it.pageId to it
                }.toMap()

                val addedPages = ArrayList<net.pantasystem.milktea.model.account.page.Page>()
                val updatedPages = ArrayList<net.pantasystem.milktea.model.account.page.Page>()

                for(page in pages){
                    when{
                        page.pageId == 0L ->{
                            addedPages.add(page)
                        }
                        page != exPageMap[page.pageId] ->{
                            updatedPages.add(page)
                        }

                    }
                }

                val removedPages = exPages.filter{
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
                publish(net.pantasystem.milktea.model.account.AccountRepository.Event.Created(account))
            }else{
                publish(net.pantasystem.milktea.model.account.AccountRepository.Event.Updated(account))
            }

            exAccount
        })

    }

    override suspend fun delete(account: net.pantasystem.milktea.model.account.Account) {
        accountDao.delete(account)
        publish(net.pantasystem.milktea.model.account.AccountRepository.Event.Deleted(account.accountId))
    }



    @Throws(net.pantasystem.milktea.model.account.AccountNotFoundException::class)
    override suspend fun get(accountId: Long): net.pantasystem.milktea.model.account.Account {
        return accountDao.getAccountRelation(accountId)?.toAccount()?: throw net.pantasystem.milktea.model.account.AccountNotFoundException(
            "$accountId を見つけられませんでした"
        )
    }

    override suspend fun findAll(): List<net.pantasystem.milktea.model.account.Account> {
        return accountDao.findAll().map{
            it.toAccount()
        }
    }

    @Throws(net.pantasystem.milktea.model.account.AccountNotFoundException::class)
    override suspend fun getCurrentAccount(): net.pantasystem.milktea.model.account.Account {
        val currentAccountId = sharedPreferences.getLong(CURRENT_ACCOUNT_ID_KEY, -1)
        val current = accountDao.getAccountRelation(currentAccountId)
        return if(current == null){
            val first = accountDao.findAll().firstOrNull()?.toAccount()
                ?: throw net.pantasystem.milktea.model.account.AccountNotFoundException("アカウントが一つも見つかりませんでした")
            setCurrentAccount(first)
        }else{
            current.toAccount()
        }

    }


    override suspend fun setCurrentAccount(account: net.pantasystem.milktea.model.account.Account): net.pantasystem.milktea.model.account.Account {
        val current = accountDao.get(account.accountId)
        val ac = if(current == null){
            add(account)
        }else{
            account
        }
        sharedPreferences.edit().also {
            it.putLong(CURRENT_ACCOUNT_ID_KEY, ac.accountId)
        }.apply()
        publish(net.pantasystem.milktea.model.account.AccountRepository.Event.Updated(ac))

        return ac
    }


    private fun publish(e: net.pantasystem.milktea.model.account.AccountRepository.Event) {
        synchronized(listeners) {
            listeners.forEach {
                it.on(e)
            }
        }
    }


}