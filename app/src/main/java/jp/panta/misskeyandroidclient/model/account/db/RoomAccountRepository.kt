package jp.panta.misskeyandroidclient.model.account.db

import android.content.SharedPreferences
import io.reactivex.Observable
import io.reactivex.Single
import jp.panta.misskeyandroidclient.model.account.*
import jp.panta.misskeyandroidclient.model.account.page.db.PageDAO

const val CURRENT_ACCOUNT_ID_KEY = "CURRENT_ACCOUNT_ID"
class RoomAccountRepository(
    private val accountDao: AccountDAO,
    private val pageDAO: PageDAO,
    private val sharedPreferences: SharedPreferences
) : AccountRepository{

    override suspend fun add(account: Account, isUpdatePages: Boolean): Account {
        var exAccount: Account? = null
        var isNeedDeepUpdate = isUpdatePages
        
        if(account.accountId > 0){
            exAccount = accountDao.get(account.accountId)
        }
        if(exAccount == null){
            exAccount = accountDao.findByUserNameAndInstanceDomain(account.userName, account.instanceDomain)?.toAccount()
        }
        if(exAccount == null){
            val id = accountDao.insert(account)
            exAccount = accountDao.get(id)?: throw AccountRegistrationFailedException()
            isNeedDeepUpdate = true
        }
        

        if(isNeedDeepUpdate){
            pageDAO.clearByAccountId(exAccount.accountId)
            pageDAO.insertAll(account.pages.mapIndexed{ i, page ->
                page.also{
                    it.weight = i
                }
            })
        }

        return exAccount
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
        val current = accountDao.get(currentAccountId)
        return if(current == null){
            val first = accountDao.findAll().firstOrNull()?.toAccount()
                ?: throw AccountNotFoundException("アカウントが一つも見つかりませんでした")
            setCurrentAccount(first)
        }else{
            current
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