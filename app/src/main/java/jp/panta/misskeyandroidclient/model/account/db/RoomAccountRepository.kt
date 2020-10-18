package jp.panta.misskeyandroidclient.model.account.db

import android.content.SharedPreferences
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.account.*
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.db.PageDAO
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.concurrent.Callable

const val CURRENT_ACCOUNT_ID_KEY = "CURRENT_ACCOUNT_ID"
class RoomAccountRepository(
    private val roomDataBase: DataBase,
    private val sharedPreferences: SharedPreferences,
    private val accountDao: AccountDAO,
    private val pageDAO: PageDAO,
) : AccountRepository{

    override suspend fun add(account: Account, isUpdatePages: Boolean): Account {
        return roomDataBase.runInTransaction(Callable<Account> {
            var exAccount: Account? = null
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
                exAccount = accountDao.get(id)?: throw AccountRegistrationFailedException()
                Log.d("RoomAccountRepository", "insertしました: $exAccount")
                isNeedDeepUpdate = true
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

                val addedPages = ArrayList<Page>()
                val updatedPages = ArrayList<Page>()

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

            }

            exAccount
        })

    }

    override suspend fun delete(account: Account) {
        accountDao.delete(account)
    }

    override suspend fun findAllByUserName(userName: String): List<Account> {
        return accountDao.findAllByUserName(userName).map{ account ->
            account.toAccount()
        }
    }


    override suspend fun findByRemoteIdAndInstanceDomain(
        remoteId: String,
        instanceDomain: String
    ): Account? {
        return accountDao.findByRemoteIdAndInstanceDomain(remoteId, instanceDomain)?.toAccount()
    }

    override suspend fun findByUserNameAndInstanceDomain(
        userName: String,
        instanceDomain: String
    ): Account? {
        return accountDao.findByUserNameAndInstanceDomain(userName, instanceDomain)?.toAccount()
    }


    @Throws(AccountNotFoundException::class)
    override suspend fun get(accountId: Long): Account {
        return accountDao.getAccountRelation(accountId)?.toAccount()?: throw AccountNotFoundException("$accountId を見つけられませんでした")
    }

    override suspend fun findAll(): List<Account> {
        return accountDao.findAll().map{
            it.toAccount()
        }
    }

    @Throws(AccountNotFoundException::class)
    override suspend fun getCurrentAccount(): Account {
        val currentAccountId = sharedPreferences.getLong(CURRENT_ACCOUNT_ID_KEY, -1)
        val current = accountDao.getAccountRelation(currentAccountId)
        return if(current == null){
            val first = accountDao.findAll().firstOrNull()?.toAccount()
                ?: throw AccountNotFoundException("アカウントが一つも見つかりませんでした")
            setCurrentAccount(first)
        }else{
            current.toAccount()
        }

    }


    override suspend fun setCurrentAccount(account: Account): Account {
        val current = accountDao.get(account.accountId)
        val ac = if(current == null){
            add(account)
        }else{
            account
        }
        sharedPreferences.edit().also {
            it.putLong(CURRENT_ACCOUNT_ID_KEY, ac.accountId)
        }.apply()

        return ac
    }



}