package jp.panta.misskeyandroidclient.model.account

import io.reactivex.Completable
import io.reactivex.Single

interface AccountRepository{

    sealed class Event {
        data class Created(val account: Account) : Event()
        data class Updated(val account: Account) : Event()
        data class Deleted(val accountId: Long) : Event()
    }

    interface Listener {
        fun on(e: Event)
    }

    suspend fun get(accountId: Long): Account

    suspend fun findByRemoteIdAndInstanceDomain(remoteId: String, instanceDomain: String): Account?

    suspend fun findByUserNameAndInstanceDomain(userName: String, instanceDomain: String): Account?

    suspend fun findAllByUserName(userName: String): List<Account>

    suspend fun add(account: Account, isUpdatePages: Boolean = false): Account

    suspend fun delete(account: Account)

    suspend fun findAll(): List<Account>

    suspend fun setCurrentAccount(account: Account): Account

    suspend fun getCurrentAccount(): Account
}
